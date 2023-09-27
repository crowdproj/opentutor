/*!
 * Page:cards js-script library.
 * To create/edit dictionary's cards.
 */

/**
 * selected dictionary resource (for edit/delete/download)
 * ```json
 *   {
 *     "dictionaryId": "2",
 *     "name": "Weather",
 *     "sourceLang": "en",
 *     "targetLang": "ru",
 *     "partsOfSpeech": [
 *       "noun",
 *       "verb",
 *       "adjective",
 *       "adverb",
 *       "pronoun",
 *       "preposition",
 *       "conjunction",
 *       "interjection",
 *       "article"
 *     ],
 *     "total": 0,
 *     "learned": 0
 *   }
 * ```
 */
let selectedDictionary;

function drawDictionaryCardsPage() {
    if (selectedDictionary == null) {
        return;
    }
    resetRowSelection($('#dictionaries tbody'));

    $('#words-title').html(selectedDictionary.name);
    $('#words tbody').html('');
    initTableListeners('words', resetCardSelection);
    $('#words-table-row').css('height', calcInitTableHeight());
    getCards(selectedDictionary.dictionaryId, initCardsTable);
}

function initCardsTable(cards) {
    const tbody = $('#words tbody');
    const search = $('#words-search');

    bootstrap.Modal.getOrCreateInstance(document.getElementById('add-card-dialog')).hide();
    bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-card-prompt')).hide();
    bootstrap.Modal.getOrCreateInstance(document.getElementById('reset-card-prompt')).hide();
    const editPopup = bootstrap.Modal.getOrCreateInstance(document.getElementById('edit-card-dialog'));
    editPopup.hide();

    initCardDialog('add', cards);
    initCardDialog('edit', cards);
    initCardPrompt('delete');
    initCardPrompt('reset');

    displayPage('words');

    toggleManageCardsButton('add', false);

    search.off('input').on('input', function () {
        resetCardSelection();
        const card = findCardByWordPrefix(cards, search.val());
        if (card == null) {
            selectCardItemForAdd(null, search.val());
            return;
        }
        scrollToRow('#w' + card.cardId, '#words-table-row', markRowSelected);
        const row = $('#w' + card.cardId);
        selectCardItemForEdit(row, card);
        selectCardItemForAdd(row, search.val());
    });

    $.each(cards, function (key, card) {
        let row = $(`<tr id="${'w' + card.cardId}">
                            <td>${getAllWordsAsString(card)}</td>
                            <td>${getAllTranslationsAsString(card)}</td>
                            <td>${percentage(card)}</td>
                          </tr>`);
        row.off('click').on('click', function () {
            cardRowOnClick(row, card);
        });
        row.off('dblclick').dblclick(function () {
            cardRowOnClick(row, card);
            editPopup.show();
        });
        tbody.append(row);
    });
}

function cardRowOnClick(row, card) {
    resetCardSelection();
    markRowSelected(row);
    selectCardItemForEdit(row, card);
    selectCardItemForAdd(row, getCardFirstWordWord(card));
    selectCardItemForDeleteOrReset(card, 'delete');
    selectCardItemForDeleteOrReset(card, 'reset');
}

function selectCardItemForEdit(row, card) {
    cleanCardDialogLinks('edit');
    const input = $('#edit-card-dialog-word');
    input.val(getCardFirstWordWord(card));
    input.attr('item-id', card.cardId);

    insertCardDialogLinks('edit');
    toggleManageCardsButton('edit', false);

    const btn = $('#edit-card-dialog-sound');
    btn.attr('word-txt', getCardFirstWordWord(card));
    if (getCardFirstWordSound(card)) {
        btn.prop('disabled', false);
        btn.attr('word-sound', getCardFirstWordSound(card));
    } else {
        btn.prop('disabled', true);
        btn.removeAttr('word-sound', getCardFirstWordSound(card));
    }

    const index = selectedDictionary.partsOfSpeech.indexOf(getCardFirstWordPartOfSpeech(card))
    if (index > -1) {
        $('#edit-card-dialog-part-of-speech option').eq(index + 1).prop('selected', true);
    }
    $('#edit-card-dialog-transcription').val(getCardFirstWordTranscriptionAsArrayArray(card));
    const translations = getCardFirstWordTranslationsAsArrayArray(card).map(x => x.join(", "));
    const examples = getCardFirstWordExamplesAsArray(card);
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
    toggleManageCardsButton('add', false);

    $('#add-card-dialog-part-of-speech option:selected').prop('selected', false);

    $('#add-card-dialog-transcription').val('');
    $('#add-card-dialog-translation').val('');
    $('#add-card-dialog-examples').val('');
}

function selectCardItemForDeleteOrReset(card, actionId) {
    toggleManageCardsButton(actionId, false);
    const body = $('#' + actionId + '-card-prompt-body');
    body.attr('item-id', card.cardId);
    body.html(getCardFirstWordWord(card));
}

function resetCardSelection() {
    toggleManageCardsButton('add', true);
    toggleManageCardsButton('edit', true);
    toggleManageCardsButton('delete', true);
    toggleManageCardsButton('reset', true);
    cleanCardDialogLinks('add');
    cleanCardDialogLinks('edit');
    resetRowSelection($('#words tbody'));
}

function toggleManageCardsButton(suffix, disable) {
    $('#words-btn-' + suffix).prop('disabled', disable);
}

function initCardDialog(dialogId, cards) {
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
    $.each(selectedDictionary.partsOfSpeech, function (index, value) {
        select.append($(`<option value="${index}">${value}</option>`));
    });

    $('#words-btn-' + dialogId).off('click').on('click', function () { // push open dialog
        onChangeCardDialogMains('edit');
        onChangeCardDialogMains('add');
    });
    $('#' + dialogId + '-card-dialog-save').off('click').on('click', function () { // push save dialog button
        const res = createResourceCardItem(dialogId, cards);
        const onDone = function (id) {
            if (id === '') {
                id = res.cardId;
            }
            drawDictionaryCardsPage();
            scrollToRow('#w' + id, '#words-table-row', markRowSelected);
        };
        if (res.cardId == null) {
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
            soundBtn.prop('disabled', true);
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
            drawDictionaryCardsPage();
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

function createResourceCardItem(dialogId, cards) {
    const input = $('#' + dialogId + '-card-dialog-word');
    const itemId = input.attr('item-id');
    const resCard = itemId ? jQuery.extend({}, findById(cards, itemId)) : {};
    resCard.dictionaryId = selectedDictionary.dictionaryId;
    setCardFirstWordWord(resCard, input.val().trim());
    setCardFirstWordTranscription(resCard, $('#' + dialogId + '-card-dialog-transcription').val().trim());
    setCardFirstWordPartOfSpeech(resCard, $('#' + dialogId + '-card-dialog-part-of-speech option:selected').text());
    setCardFirstWordExamplesArray(resCard, toArray($('#' + dialogId + '-card-dialog-examples').val(), '\n'));
    setCardFirstWordTranslationsArrayArray(resCard,
        toArray($('#' + dialogId + '-card-dialog-translation').val(), '\n').map(x => toArray(x, ',')));
    return resCard;
}

function cleanCardDialogLinks(dialogId) {
    $('#' + dialogId + '-card-dialog-gl-link').html('');
    $('#' + dialogId + '-card-dialog-yq-link').html('');
    $('#' + dialogId + '-card-dialog-lg-link').html('');
    $('#' + dialogId + '-card-dialog-lg-collapse').removeClass('show');
}

function insertCardDialogLinks(dialogId) {
    const input = $('#' + dialogId + '-card-dialog-word');
    const sl = selectedDictionary.sourceLang;
    const tl = selectedDictionary.targetLang;
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
    const sl = selectedDictionary.sourceLang;
    const tl = selectedDictionary.targetLang;

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

function findCardByWordPrefix(cards, prefix) {
    if (prefix === null || prefix === undefined) {
        return null;
    }
    const search = prefix.trim().toLowerCase();
    if (search.length === 0) {
        return null;
    }
    return cards.find((card) => getCardFirstWordWord(card).toLowerCase().startsWith(search));
}