/*!
 * page:cards js-script library.
 */

function drawDictionaryPage() {
    if (dictionary == null) {
        return;
    }
    resetRowSelection($('#dictionaries tbody'));

    $('#words-title').html(dictionary.name);
    $('#words tbody').html('');
    initTableListeners('words', resetCardSelection);
    $('#words-table-row').css('height', calcInitTableHeight());
    getCards(dictionary.id, initCardsTable);
}

function initCardsTable(items) {
    const tbody = $('#words tbody');
    const search = $('#words-search');

    bootstrap.Modal.getOrCreateInstance(document.getElementById('add-card-dialog')).hide();
    bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-card-prompt')).hide();
    bootstrap.Modal.getOrCreateInstance(document.getElementById('reset-card-prompt')).hide();
    const editPopup = bootstrap.Modal.getOrCreateInstance(document.getElementById('edit-card-dialog'));
    editPopup.hide();

    initCardDialog('add', items);
    initCardDialog('edit', items);
    initCardPrompt('delete');
    initCardPrompt('reset');

    displayPage('words');

    search.off('input').on('input', function () {
        resetCardSelection();
        const item = findItem(items, search.val());
        if (item == null) {
            selectCardItemForAdd(null, search.val());
            return;
        }
        scrollToRow('#w' + item.id, '#words-table-row', markRowSelected);
        const row = $('#w' + item.id);
        selectCardItemForEdit(row, item);
        selectCardItemForAdd(row, search.val());
    });

    $.each(items, function (key, item) {
        let row = $(`<tr id="${'w' + item.id}">
                            <td>${item.word}</td>
                            <td>${toTranslationString(item)}</td>
                            <td>${percentage(item)}</td>
                          </tr>`);
        row.off('click').on('click', function () {
            cardRowOnClick(row, item);
        });
        row.off('dblclick').dblclick(function () {
            cardRowOnClick(row, item);
            editPopup.show();
        });
        tbody.append(row);
    });
}

function cardRowOnClick(row, item) {
    resetCardSelection();
    markRowSelected(row);
    selectCardItemForEdit(row, item);
    selectCardItemForAdd(row, item.word);
    selectCardItemForDeleteOrReset(item, 'delete');
    selectCardItemForDeleteOrReset(item, 'reset');
}

function selectCardItemForEdit(row, item) {
    cleanCardDialogLinks('edit');
    const input = $('#edit-card-dialog-word');
    input.val(item.word);
    input.attr('item-id', item.id);

    insertCardDialogLinks('edit');
    disableCardButton('edit', false);

    const btn = $('#edit-card-dialog-sound');
    btn.attr('word-txt', item.word);
    btn.attr('word-sound', item.sound);

    const index = dictionary.partsOfSpeech.indexOf(item.partOfSpeech)
    if (index > -1) {
        $('#edit-card-dialog-part-of-speech option').eq(index + 1).prop('selected', true);
    }
    $('#edit-card-dialog-transcription').val(item.transcription);
    const translations = item.translations.map(x => x.join(", "));
    const examples = item.examples;
    const translationsArea = $('#edit-card-dialog-translation');
    const examplesArea = $('#edit-card-dialog-examples');
    translationsArea.attr('rows', translations.length);
    examplesArea.attr('rows', examples.length);
    translationsArea.val(translations.join("\n"));
    examplesArea.val(examples.join("\n"));
}

function selectCardItemForAdd(row, word) {
    cleanCardDialogLinks('add');
    if (row != null) {
        markRowSelected(row);
    }
    $('#add-card-dialog-word').val(word);
    insertCardDialogLinks('add');
    disableCardButton('add', false);

    $('#add-card-dialog-part-of-speech option:selected').prop('selected', false);

    $('#add-card-dialog-transcription').val('');
    $('#add-card-dialog-translation').val('');
    $('#add-card-dialog-examples').val('');
}

function selectCardItemForDeleteOrReset(item, actionId) {
    disableCardButton(actionId, false);
    const body = $('#' + actionId + '-card-prompt-body');
    body.attr('item-id', item.id);
    body.html(item.word);
}

function resetCardSelection() {
    disableCardButton('add', true);
    disableCardButton('edit', true);
    disableCardButton('delete', true);
    disableCardButton('reset', true);
    cleanCardDialogLinks('add');
    cleanCardDialogLinks('edit');
    resetRowSelection($('#words tbody'));
}

function disableCardButton(suffix, disable) {
    $('#words-btn-' + suffix).prop('disabled', disable);
}

function initCardDialog(dialogId, items) {
    $('#' + dialogId + '-card-dialog-lg-collapse').off('show.bs.collapse').on('show.bs.collapse', function () {
        onCollapseLgFrame(dialogId);
    });
    $('#' + dialogId + '-card-dialog-word').off('input').on('input', function () {
        onChangeCardDialogMains(dialogId);
        insertCardDialogLinks(dialogId);
    });
    $('#' + dialogId + '-card-dialog-translation').off('input').on('input', function () {
        onChangeCardDialogMains(dialogId);
    });
    const select = $('#' + dialogId + '-card-dialog-part-of-speech').html('').append($(`<option value="-1"></option>`));
    $.each(dictionary.partsOfSpeech, function (index, value) {
        select.append($(`<option value="${index}">${value}</option>`));
    });

    $('#words-btn-' + dialogId).off('click').on('click', function () { // push open dialog
        onChangeCardDialogMains('edit');
        onChangeCardDialogMains('add');
    });
    $('#' + dialogId + '-card-dialog-save').off('click').on('click', function () { // push save dialog button
        const res = createResourceCardItem(dialogId, items);
        const onDone = function (id) {
            if (id === '') {
                id = res.id;
            }
            drawDictionaryPage();
            scrollToRow('#w' + id, '#words-table-row', markRowSelected);
        };
        if (res.id == null) {
            createCard(res, onDone);
        } else {
            updateCard(res, onDone);
        }
    });
    if ('edit' === dialogId) {
        initCardEditDialog();
    }
}

function initCardEditDialog() {
    const soundBtn = $('#edit-card-dialog-sound');
    soundBtn.prop('disabled', false);
    soundBtn.off('click').on('click', function () {
        const audio = soundBtn.attr('word-sound');
        if (!audio) {
            return;
        }
        soundBtn.prop('disabled', true);
        playAudio(audio, function () {
            soundBtn.prop('disabled', false);
        });
    });
}

function initCardPrompt(actionId) {
    $('#' + actionId + '-card-prompt-confirm').off('click').on('click', function () {
        const body = $('#' + actionId + '-card-prompt-body');
        const id = body.attr('item-id');
        if (!id) {
            return;
        }
        const onDone = function () {
            drawDictionaryPage();
            if (actionId !== 'delete') {
                scrollToRow('#w' + id, '#words-table-row', markRowSelected);
            }
        };
        if (actionId === 'delete') {
            deleteCard(id, onDone);
        } else {
            resetCard(id, onDone);
        }
    });
}

function onChangeCardDialogMains(dialogId) {
    const word = $('#' + dialogId + '-card-dialog-word');
    const translation = $('#' + dialogId + '-card-dialog-translation');
    $('#' + dialogId + '-card-dialog-save').prop('disabled', !(word.val() && translation.val()));
}

function createResourceCardItem(dialogId, items) {
    const input = $('#' + dialogId + '-card-dialog-word');

    const itemId = input.attr('item-id');

    const resItem = itemId ? jQuery.extend({}, findById(items, itemId)) : {};
    resItem.dictionaryId = dictionary.id;
    resItem.word = input.val().trim();
    resItem.transcription = $('#' + dialogId + '-card-dialog-transcription').val().trim();
    resItem.partOfSpeech = $('#' + dialogId + '-card-dialog-part-of-speech option:selected').text();
    resItem.examples = toArray($('#' + dialogId + '-card-dialog-examples').val(), '\n');
    resItem.translations = toArray($('#' + dialogId + '-card-dialog-translation').val(), '\n')
        .map(x => toArray(x, ','));
    return resItem;
}

function cleanCardDialogLinks(dialogId) {
    $('#' + dialogId + '-card-dialog-gl-link').html('');
    $('#' + dialogId + '-card-dialog-yq-link').html('');
    $('#' + dialogId + '-card-dialog-lg-link').html('');
    $('#' + dialogId + '-card-dialog-lg-collapse').removeClass('show');
}

function insertCardDialogLinks(dialogId) {
    const input = $('#' + dialogId + '-card-dialog-word');
    const sl = dictionary.sourceLang;
    const tl = dictionary.targetLang;
    const text = input.val();
    if (!text) {
        return;
    }
    createLink($('#' + dialogId + '-card-dialog-gl-link'), toGlURI(text, sl, tl));
    createLink($('#' + dialogId + '-card-dialog-ya-link'), toYaURI(text, sl, tl));
    createLink($('#' + dialogId + '-card-dialog-lg-link'), toLgURI(text, sl, tl));
}

function createLink(parent, uri) {
    parent.html(`<a class='btn btn-link' href='${uri}' target='_blank'>${uri}</a>`);
}

function onCollapseLgFrame(dialogId) {
    const sl = dictionary.sourceLang;
    const tl = dictionary.targetLang;

    const lgDiv = $('#' + dialogId + '-card-dialog-lg-collapse div');
    const dialogLinksDiv = $('#' + dialogId + '-card-dialog-links');
    const wordInput = $('#' + dialogId + '-card-dialog-word');
    const text = wordInput.val();
    const prev = dialogLinksDiv.attr('word-txt');
    if (text === prev) {
        return;
    }
    const extUri = toLgURI(text, sl, tl);
    const height = calcInitLgFrameHeight();
    const frame = $(`<iframe noborder="0" width="1140" height="800">LgFrame</iframe>`);
    frame.attr('src', extUri);
    frame.attr('height', height);
    lgDiv.html('').append(frame);
    dialogLinksDiv.attr('word-txt', text);
}
