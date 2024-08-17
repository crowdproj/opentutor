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

    const headers = $('#words-table-row th');
    const thWord = $(headers[0]);
    const thTranslation = $(headers[1]);
    const thStatus = $(headers[2]);
    thWord.off('click').on('click', function () {
        const direction = sortDirection(thWord);
        cards.sort((a, b) => {
            const left = getAllWordsAsString(a);
            const right = getAllWordsAsString(b);
            return direction ? left.localeCompare(right) : right.localeCompare(left);
        });
        drawCardsTable(cards, editPopup);
    });
    thTranslation.off('click').on('click', function () {
        const direction = sortDirection(thTranslation);
        cards.sort((a, b) => {
            const left = getAllTranslationsAsString(a);
            const right = getAllTranslationsAsString(b);
            return direction ? left.localeCompare(right) : right.localeCompare(left);
        });
        drawCardsTable(cards, editPopup);
    });
    thStatus.off('click').on('click', function () {
        const direction = sortDirection(thStatus);
        cards.sort((a, b) => {
            const left = percentage(a);
            const right = percentage(b);
            return direction ? left - right : right - left;
        });
        drawCardsTable(cards, editPopup);
    });

    drawCardsTable(cards, editPopup);
}

function drawCardsTable(cards, editPopup) {
    const tbody = $('#words tbody');
    tbody.html('');
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
    const index = 1
    cleanCardDialogLinks('edit', index);
    removeExtraAccordionItems('edit');

    $.each(card.words, function (i, cardWord) {
        const index = i + 1;

        if (i !== 0) {
            initNextCardDialogAccordionItem('edit', i);
        }
        if (i === card.words.length - 1) {
            $('#edit-card-dialog-collapse-i' + index + ' .add-word').show();
            if (i !== 0) {
                $('#edit-card-dialog-collapse-i' + index + ' .remove-word').show();
            }
        }

        const wordInput = $('#edit-card-dialog-collapse-i' + index + ' .word');
        wordInput.val(cardWord.word);
        wordInput.attr('item-id', card.cardId);

        insertCardDialogLinks('edit', index);

        const soundBtn = $('#edit-card-dialog-collapse-i' + index + ' .sound');
        soundBtn.attr('word-txt', cardWord.word);
        if (cardWord.sound) {
            soundBtn.prop('disabled', false);
            soundBtn.attr('word-sound', cardWord.sound);
            initEditCardDialogSound(index);
        } else {
            soundBtn.prop('disabled', true);
            soundBtn.removeAttr('word-sound', cardWord.sound);
        }

        const selectIndex = selectedDictionary.partsOfSpeech.indexOf(cardWord.partOfSpeech)
        if (selectIndex > -1) {
            $('#edit-card-dialog-collapse-i' + index + ' .part-of-speech option').eq(index).prop('selected', true);
        }
        $('#edit-card-dialog-collapse-i' + index + ' .transcription').val(cardWord.transcription);

        const translations = cardWord.translations.map(x => x.join(", "));
        const examples = cardWord.examples.map(it => it.example);
        const translationsArea = $('#edit-card-dialog-collapse-i' + index + ' .translation');
        const examplesArea = $('#edit-card-dialog-collapse-i' + index + ' .examples');
        translationsArea.attr('rows', translations.length);
        examplesArea.attr('rows', examples.length);
        translationsArea.val(translations.join("\n"));
        examplesArea.val(examples.join("\n"));

    });
    closeAccordionItemsWithExcept('edit', 1);

    toggleManageCardsButton('edit', false);
}

function selectCardItemForAdd(row, word) {
    const index = 1
    cleanCardItemMains('add', index);
    if (row != null) {
        markRowSelected(row);
    }
    $('#add-card-dialog-collapse-i' + index + ' .word').val(word);
    insertCardDialogLinks('add', index);
    toggleManageCardsButton('add', false);
    removeExtraAccordionItems('add');
    $('#add-card-dialog-collapse-i' + index + ' .add-word').show();
}

function selectCardItemForDeleteOrReset(card, actionId) {
    toggleManageCardsButton(actionId, false);
    const body = $('#' + actionId + '-card-prompt-body');
    body.attr('item-id', card.cardId);
    body.html(getAllWordsAsString(card));
}

function resetCardSelection() {
    toggleManageCardsButton('add', true);
    toggleManageCardsButton('edit', true);
    toggleManageCardsButton('delete', true);
    toggleManageCardsButton('reset', true);
    cleanCardDialogLinks('add', 1);
    cleanCardDialogLinks('edit', 1);
    resetRowSelection($('#words tbody'));

    removeExtraAccordionItems('add');
    removeExtraAccordionItems('edit');
}

function toggleManageCardsButton(suffix, disable) {
    $('#words-btn-' + suffix).prop('disabled', disable);
}

function initCardDialog(dialogId, cards) {
    const index = 1
    // full screen handler
    $('#' + dialogId + '-card-dialog-fullscreen-btn').off('click').on('click', function () {
        const dialog = $('#' + dialogId + '-card-dialog .modal-dialog')
        dialog.toggleClass('modal-fullscreen');
        const icon = $(this).find('i');
        if (dialog.hasClass('modal-fullscreen')) {
            icon.removeClass('bi-arrows-fullscreen').addClass('bi-arrows-angle-contract');
        } else {
            icon.removeClass('bi-arrows-angle-contract').addClass('bi-arrows-fullscreen');
        }
    });

    initCardDialogAccordionItemInputs(dialogId, index)

    $('#words-btn-' + dialogId).off('click').on('click', function () { // push open dialog
        removeExtraAccordionItems('add');
        closeAccordionItemsWithExcept('edit', 1);
        closeAccordionItemsWithExcept('add', 1);
        onChangeCardDialogMains('edit', index);
        onChangeCardDialogMains('add', index);
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

    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .add-word').off('click').on('click', function () {
        initNextCardDialogAccordionItem(dialogId, index);
        closeAccordionItemsWithExcept(dialogId, index + 1);
    });
}

function initNextCardDialogAccordionItem(dialogId, prevIndex) {
    const index = prevIndex + 1

    // create new item
    cloneCardDialogAccordionItem(dialogId, prevIndex, index);

    // inputs
    initCardDialogAccordionItemInputs(dialogId, index);

    // init remove-word button
    initCardDialogAccordionItemRemoveButton(dialogId, prevIndex, index);

    // init add-word button
    initCardDialogAccordionItemAddButton(dialogId, index);
}

function cloneCardDialogAccordionItem(dialogId, sourceIndex, targetIndex) {
    const nextItem = $('#' + dialogId + '-card-dialog-accordion-item-i' + sourceIndex).clone();
    updateCardDialogAccordionItemIds(nextItem, sourceIndex, targetIndex);
    $('#' + dialogId + '-card-dialog-accordion').append(nextItem);
    nextItem.find('.accordion-button').text('Word #' + targetIndex);
    cleanCardItemMains(dialogId, targetIndex);
}

function initCardDialogAccordionItemRemoveButton(dialogId, prevIndex, index) {
    $('.remove-word').each(function () {
        $(this).hide();
    })
    const removeWordButton = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .remove-word')
    const prevRemoveWordButton = $('#' + dialogId + '-card-dialog-collapse-i' + prevIndex + ' .remove-word')
    const prevAddWordButton = $('#' + dialogId + '-card-dialog-collapse-i' + prevIndex + ' .add-word')
    removeWordButton.on('click', function () {
        $(this).closest('.accordion-item').remove();
        if (prevIndex !== 1) {
            prevRemoveWordButton.show();
        }
        prevAddWordButton.show();
        closeAccordionItemsWithExcept(dialogId, prevIndex);
    });
    removeWordButton.show();
}

function initCardDialogAccordionItemAddButton(dialogId, index) {
    $('.add-word').each(function () {
        $(this).hide();
    })
    const addWordButton = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .add-word')
    addWordButton.off('click').on('click', function () {
        initNextCardDialogAccordionItem(dialogId, index);
        closeAccordionItemsWithExcept(dialogId, index + 1);
    });
    onChangeCardDialogMains(dialogId, index);
}

function initCardDialogAccordionItemInputs(dialogId, index) {
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .lg-link-collapse')
        .off('show.bs.collapse')
        .on('show.bs.collapse', function () {
            onCollapseLgFrame(dialogId, index);
        });
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .word')
        .off('input')
        .on('input', function () {
            onChangeCardDialogMains(dialogId, index);
            insertCardDialogLinks(dialogId, index);
        });
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .translation')
        .off('input')
        .on('input', function () {
            onChangeCardDialogMains(dialogId, index);
        });
    const select = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .part-of-speech')
        .html('').append($(`<option value="-1"></option>`));
    $.each(selectedDictionary.partsOfSpeech, function (i, value) {
        select.append($(`<option value="${i}">${value}</option>`));
    });
}

function closeAccordionItemsWithExcept(dialogId, index) {
    $('#' + dialogId + '-card-dialog .accordion-button').each(function () {
        if ($(this).attr('data-bs-target').includes('-i' + index)) {
            if ($(this).hasClass('collapsed')) {
                $(this).click();
            }
        } else {
            if (!$(this).hasClass('collapsed')) {
                $(this).click();
            }
        }
    });
}

function initEditCardDialogSound(index) {
    const soundBtn = $('#edit-card-dialog-collapse-i' + index + ' .sound');
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

function onChangeCardDialogMains(dialogId, index) {
    const word = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .word');
    const translation = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .translation');
    const save = $('#' + dialogId + '-card-dialog-save');

    if (index === 1) {
        save.prop('disabled', !(word.val() && translation.val()));
    } else {
        save.prop('disabled', !word.val());
    }

    const addWordButton = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .add-word');
    if (!isLastAccordionItem(dialogId, index)) {
        // not last
        addWordButton.hide();
        return;
    }
    if (index === 1) {
        if (word.val() && translation.val()) {
            addWordButton.show();
        } else {
            addWordButton.hide();
        }
    } else {
        if (word.val()) {
            addWordButton.show();
        } else {
            addWordButton.hide();
        }
    }
}

function isLastAccordionItem(dialogId, index) {
    return $('.accordion-item').find('#' + dialogId + '-card-dialog-collapse-i' + (index + 1)).length === 0;
}

function createResourceCardItem(dialogId, cards) {
    const itemId = $('#' + dialogId + '-card-dialog-collapse-i1 .word').attr('item-id');

    const resCard = itemId ? jQuery.extend({}, findById(cards, itemId)) : {};
    resCard.dictionaryId = selectedDictionary.dictionaryId;
    resCard.words = [];

    let i = 0
    $('#' + dialogId + '-card-dialog .accordion-item').each(function () {
        const wordInput = $(this).find('.word');
        const transcriptionInput = $(this).find('.transcription');
        const partOfSpeechInput = $(this).find('.part-of-speech option:selected');
        const examplesInput = $(this).find('.examples');
        const translationInput = $(this).find('.translation');
        if (!wordInput.val()) {
            return;
        }
        if (i === 0 && !translationInput.val()) {
            return;
        }

        const w = {};
        w.word = wordInput.val().trim();
        w.transcription = transcriptionInput.val().trim();
        w.partOfSpeech = partOfSpeechInput.text();
        w.examples = []
        const examples = toArray(examplesInput.val(), '\n');
        examples.forEach(function (example) {
            let ex = {example: example}
            w.examples.push(ex)
        })
        w.translations = toArray(translationInput.val(), '\n').map(x => toArray(x, '[,;]'));

        resCard.words[i] = w;
        i++;
    });

    return resCard;
}

function cleanCardItemMains(dialogId, index) {
    cleanCardDialogLinks(dialogId, index);
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .word').val('');
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .part-of-speech option:selected').prop('selected', false);
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .transcription').val('');
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .translation').val('');
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .examples').val('');
}

function cleanCardDialogLinks(dialogId, index) {
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .gl-link').html('');
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .ya-link').html('');
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .lg-link').html('');
    $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .lg-link-collapse').removeClass('show');
}

function removeExtraAccordionItems(dialogId) {
    $('#' + dialogId + '-card-dialog .accordion-item').each(function () {
        if ($(this).attr('id').includes('-i1')) {
            const accordionButton = $(this).find('.accordion-button');
            if (accordionButton.hasClass('collapsed')) {
                accordionButton.click();
            }
        } else {
            $(this).remove();
        }
    });
}

function insertCardDialogLinks(dialogId, index) {
    const input = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .word');
    const sl = selectedDictionary.sourceLang;
    const tl = selectedDictionary.targetLang;
    const text = input.val();
    if (!text) {
        return;
    }
    createLink($('#' + dialogId + '-card-dialog-collapse-i' + index + ' .gl-link'), toGlURI(text, sl, tl));
    createLink($('#' + dialogId + '-card-dialog-collapse-i' + index + ' .ya-link'), toYaURI(text, sl, tl));
    createLink($('#' + dialogId + '-card-dialog-collapse-i' + index + ' .lg-link'), toLgURI(text, sl, tl));
}

function createLink(parent, uri) {
    parent.html(`<a class='btn btn-link' href='${uri}' target='_blank'>${uri}</a>`);
}

function onCollapseLgFrame(dialogId, index) {
    const sl = selectedDictionary.sourceLang;
    const tl = selectedDictionary.targetLang;

    const lgDiv = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .lg-link-collapse div');
    const dialogLinksDiv = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .links');
    const wordInput = $('#' + dialogId + '-card-dialog-collapse-i' + index + ' .word');
    const text = wordInput.val();
    const prev = dialogLinksDiv.attr('word-txt');
    if (text === prev) {
        return;
    }
    const extUri = toLgURI(text, sl, tl);
    const height = calcInitLgFrameHeight();
    // noinspection HtmlUnknownAttribute
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
    return cards.find((card) => getAllWordsAsString(card).toLowerCase().startsWith(search));
}

function updateCardDialogAccordionItemIds(element, indexWas, indexNew) {
    $(element).attr('id', $(element).attr('id').replace('i' + indexWas, 'i' + indexNew));
    $(element).find('[id]').each(function () {
        const id = $(this).attr('id');
        $(this).attr('id', id.replace('i' + indexWas, 'i' + indexNew));
    });
    $(element).find('[data-bs-target]').each(function () {
        const target = $(this).attr('data-bs-target');
        $(this).attr('data-bs-target', target.replace('i' + indexWas, 'i' + indexNew));
    });
    $(element).find('[aria-controls]').each(function () {
        const controls = $(this).attr('aria-controls');
        $(this).attr('aria-controls', controls.replace('i' + indexWas, 'i' + indexNew));
    });
    $(element).find('[aria-labelledby]').each(function () {
        const labelledby = $(this).attr('aria-labelledby');
        $(this).attr('aria-labelledby', labelledby.replace('i' + indexWas, 'i' + indexNew));
    });
}