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
    getCards(selectedDictionary.dictionaryId, function (cards) {
        initCardsTable(cards, selectedDictionary.numberOfRightAnswers);
    });
}

function initCardsTable(cards, numberOfRightAnswers) {
    const searchInput = $('#words-search');

    bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-card-prompt')).hide();
    bootstrap.Modal.getOrCreateInstance(document.getElementById('reset-card-prompt')).hide();
    const cardPopup = bootstrap.Modal.getOrCreateInstance(document.getElementById('card-dialog'));
    cardPopup.hide();

    initCardDialog(cards);
    initCardPrompt('delete');
    initCardPrompt('reset');

    displayPage('words');

    toggleManageCardsButton('add', false);

    searchInput.off('input').on('input', function () {
        resetCardSelection();
        const word = searchInput.val();
        const card = findCardByWordPrefix(cards, word);
        if (card != null) {
            scrollToRow(`#w${card.cardId}`, '#words-table-row', function (row) {
                markRowSelected(row);
                toggleManageCardsButton('edit', false);
                toggleManageCardsButton('delete', false);
                toggleManageCardsButton('reset', false);
            });
        }
    });

    const headers = $('#words-table-row th');
    const thWord = $(headers[0]);
    const thTranslation = $(headers[1]);
    const thStatus = $(headers[2]);
    thWord.off('click').on('click', function () {
        const direction = sortDirection(thWord);
        sortCardsByWord(cards, direction);
        drawCardsTable(cards, cardPopup, numberOfRightAnswers);
    });
    thTranslation.off('click').on('click', function () {
        const direction = sortDirection(thTranslation);
        sortCardsByTranslation(cards, direction);
        drawCardsTable(cards, cardPopup, numberOfRightAnswers);
    });
    thStatus.off('click').on('click', function () {
        const direction = sortDirection(thStatus);
        sortCardsByStatus(cards, direction, numberOfRightAnswers);
        drawCardsTable(cards, cardPopup, numberOfRightAnswers);
    });

    sortCardsByWord(cards, true);
    drawCardsTable(cards, cardPopup, numberOfRightAnswers);
}

/**
 * Draws the card table by populating the table body with rows representing each card.
 * Attaches click and double-click event handlers to each row for interaction.
 *
 * @param {Array} cards - An array of card objects to be displayed in the table.
 * @param {Object} popup - A popup object that is triggered for editing actions.
 * @param {number} numberOfRightAnswers - The number of right answers used to calculate the percentage.
 * @return {void}
 */
function drawCardsTable(cards, popup, numberOfRightAnswers) {
    const tbody = $('#words tbody');
    tbody.html('');
    $.each(cards, function (key, card) {
        let row = $(`<tr id="w${card.cardId}">
                            <td>${getCardWord(card)}</td>
                            <td>${getTranslationsAsString(card)}</td>
                            <td>${percentage(card, numberOfRightAnswers)}</td>
                          </tr>`);
        row.off('click').on('click', function () {
            cardRowOnClick(row, card);
        });
        row.off('dblclick').dblclick(function () {
            cardRowOnClick(row, card);
            popup.show();
        });
        tbody.append(row);
    });
}

function sortCardsByWord(cards, direction) {
    cards.sort((a, b) => {
        const left = getCardWord(a);
        const right = getCardWord(b);
        return direction ? left.localeCompare(right) : right.localeCompare(left);
    });
}

function sortCardsByTranslation(cards, direction) {
    cards.sort((a, b) => {
        const left = getTranslationsAsString(a);
        const right = getTranslationsAsString(b);
        return direction ? left.localeCompare(right) : right.localeCompare(left);
    });
}

function sortCardsByStatus(cards, direction, numberOfRightAnswers) {
    cards.sort((a, b) => {
        const left = percentage(a, numberOfRightAnswers);
        const right = percentage(b, numberOfRightAnswers);
        return direction ? left - right : right - left;
    });
}

/**
 * @param row {Object} Jquery row object
 * @param card {Object} card object
 * @return {void}
 */
function cardRowOnClick(row, card) {
    resetCardSelection();
    toggleManageCardsButton('edit', false);
    toggleManageCardsButton('delete', false);
    toggleManageCardsButton('reset', false);
    markRowSelected(row);
    selectCardItemForDeleteOrReset(card, 'delete');
    selectCardItemForDeleteOrReset(card, 'reset');
    fillCardDialogForm(card);
}

/**
 * Configures and displays the card dialog elements for editing a card's details.
 * It initializes and populates UI components with the card's data, such as words, sounds, parts of speech,
 * transcriptions, translations, and examples.
 * It also manages the visibility and state of UI elements
 * within the card editing interface.
 *
 * @param {Object} card - The card object containing details of the card such as words,
 * sounds, and details required for populating the card editing dialog.
 * @return {void} This function does not return a value but modifies UI elements.
 */
function fillCardDialogForm(card) {
    cleanCardDialogLinks(1);
    removeExtraAccordionItems();

    const title = $('#card-dialog-label');
    if (card.cardId !== undefined) {
        title.html('EDIT');
    } else {
        title.html('ADD');
    }

    $.each(card.words, function (i, word) {
        const index = i + 1;

        if (i !== 0) {
            initNextCardDialogAccordionItem(i);
        }
        if (i === card.words.length - 1) {
            $(`#card-dialog-collapse-i${index} .add-word`).show();
            if (i !== 0) {
                $(`#card-dialog-collapse-i${index} .remove-word`).show();
            }
        }

        $(`#card-dialog-collapse-i${index} .primary-word`).prop('checked', word.primary === true);

        const wordInput = $(`#card-dialog-collapse-i${index} .word`);
        wordInput.val(word.word);
        if (card.cardId !== undefined) {
            wordInput.attr('item-id', card.cardId);
        } else {
            wordInput.removeAttr('item-id');
        }

        insertCardDialogLinks(index);

        const soundBtn = $(`#card-dialog-collapse-i${index} .sound`);
        soundBtn.attr('word-txt', word.word);
        if (word.sound) {
            soundBtn.prop('disabled', false);
            soundBtn.attr('word-sound', word.sound);
            initEditCardDialogSound(index);
        } else {
            soundBtn.prop('disabled', true);
            soundBtn.removeAttr('word-sound');
        }

        const selectIndex = selectedDictionary.partsOfSpeech.indexOf(word.partOfSpeech)
        if (selectIndex > -1) {
            $(`#card-dialog-collapse-i${index} .part-of-speech option`).eq(index).prop('selected', true);
        }
        $(`#card-dialog-collapse-i${index} .transcription`).val(word.transcription);

        const translations = word.translations.map(x => x.join(", "));
        const translationsArea = $(`#card-dialog-collapse-i${index} .translation`);
        translationsArea.attr('rows', translations.length);
        translationsArea.val(translations.join("\n"));

        const examples = word.examples.map(it => it.example);
        const examplesArea = $(`#card-dialog-collapse-i${index} .examples`);
        examplesArea.attr('rows', examples.length);
        examplesArea.val(examples.join("\n"));
    });
    closeAccordionItemsWithExcept(1);
}

function selectCardItemForDeleteOrReset(card, actionId) {
    toggleManageCardsButton(actionId, false);
    const body = $(`#${actionId}-card-prompt-body`);
    body.attr('item-id', card.cardId);
    body.html(getCardWord(card));
}

function resetCardSelection() {
    toggleManageCardsButton('add', false);
    toggleManageCardsButton('edit', true);
    toggleManageCardsButton('delete', true);
    toggleManageCardsButton('reset', true);
    cleanCardDialogLinks(1);
    resetRowSelection($('#words tbody'));
    removeExtraAccordionItems();
}

function toggleManageCardsButton(suffix, disable) {
    $(`#words-btn-${suffix}`).prop('disabled', disable);
}

function initCardDialog(cards) {
    const sectionIndex = 1
    // full-screen handler
    $('#card-dialog-fullscreen-btn').off('click').on('click', function () {
        const dialog = $('#card-dialog .modal-dialog');
        dialog.toggleClass('modal-fullscreen');
        const icon = $(this).find('i');
        if (dialog.hasClass('modal-fullscreen')) {
            icon.removeClass('bi-arrows-fullscreen').addClass('bi-arrows-angle-contract');
        } else {
            icon.removeClass('bi-arrows-angle-contract').addClass('bi-arrows-fullscreen');
        }
    });

    initCardDialogAccordionItemInputs(sectionIndex)

    $('#words-btn-edit').off('click').on('click', function () { // edit dialog
        const cardId = selectedRow().attr('id').replace('w', '')
        const card = findById(cards, cardId)
        fillCardDialogForm(card);
        onChangeCardDialogMains(1);
    });
    $('#words-btn-add').off('click').on('click', function () { // add dialog
        const searchInput = $('#words-search');
        const word = searchInput.val();
        const newCard = {
            dictionaryId: selectedDictionary.dictionaryId,
            words: [
                {
                    word: word,
                    translations: [],
                    examples: []
                }
            ]
        };
        if (word !== '') {
            let fetched = false;
            fetchTranslation(selectedDictionary.sourceLang, selectedDictionary.targetLang, word, function (fetchedCard) {
                fetched = true;
                if (isEmptyCard(fetchedCard)) {
                    activateDialogOnPushAddCardButton(newCard);
                } else {
                    activateDialogOnPushAddCardButton(fetchedCard);
                }
            });
            if (fetched) {
                return;
            }
        }
        activateDialogOnPushAddCardButton(newCard);
    });
    $('#card-dialog-save').off('click').on('click', function () { // push 'save' dialog button
        const res = createResourceCardItem(cards);
        const onDone = function (card) {
            drawDictionaryCardsPage();
            scrollToRow('#w' + card.cardId, '#words-table-row', function (row) {
                cardRowOnClick(row, card);
            });
            const searchInput = $('#words-search');
            searchInput.val('');
        };
        if (res.cardId == null) {
            createCard(res, onDone);
        } else {
            updateCard(res, onDone);
        }
    });

    $(`#card-dialog-collapse-i${sectionIndex} .add-word`).off('click').on('click', function () {
        initNextCardDialogAccordionItem(sectionIndex);
        closeAccordionItemsWithExcept(sectionIndex + 1);
    });
}

function activateDialogOnPushAddCardButton(card) {
    markRowUnselected(selectedRow());
    toggleManageCardsButton('edit', true);
    toggleManageCardsButton('delete', true);
    toggleManageCardsButton('reset', true);
    fillCardDialogForm(card);
    onChangeCardDialogMains(1);
}

function initNextCardDialogAccordionItem(prevIndex) {
    const index = prevIndex + 1

    // create new item
    cloneCardDialogAccordionItem(prevIndex, index);

    // inputs
    initCardDialogAccordionItemInputs(index);

    // init remove-word button
    initCardDialogAccordionItemRemoveButton(prevIndex, index);

    // init add-word button
    initCardDialogAccordionItemAddButton(index);
}

function cloneCardDialogAccordionItem(sourceIndex, targetIndex) {
    const nextItem = $(`#card-dialog-accordion-item-i${sourceIndex}`).clone();
    updateCardDialogAccordionItemIds(nextItem, sourceIndex, targetIndex);
    $('#card-dialog-accordion').append(nextItem);
    nextItem.find('.accordion-button').text(`Word #${targetIndex}`);
    cleanCardItemMains(targetIndex);
}

function initCardDialogAccordionItemRemoveButton(prevIndex, index) {
    $('.remove-word').each(function () {
        $(this).hide();
    })
    const removeWordButton = $(`#card-dialog-collapse-i${index} .remove-word`)
    const prevRemoveWordButton = $(`#card-dialog-collapse-i${prevIndex} .remove-word`)
    const prevAddWordButton = $(`#card-dialog-collapse-i${prevIndex} .add-word`)
    const save = $('#card-dialog-save');
    removeWordButton.off('click').on('click', function () {
        const word = $(`#card-dialog-collapse-i${index} .word`);
        save.prop('disabled', !word.val());
        $(this).closest('.accordion-item').remove();
        if (prevIndex !== 1) {
            prevRemoveWordButton.show();
        }
        prevAddWordButton.show();
        closeAccordionItemsWithExcept(prevIndex);
    });
    removeWordButton.show();
}

function initCardDialogAccordionItemAddButton(index) {
    $('.add-word').each(function () {
        $(this).hide();
    })
    const addWordButton = $(`#card-dialog-collapse-i${index} .add-word`)
    addWordButton.off('click').on('click', function () {
        initNextCardDialogAccordionItem(index);
        closeAccordionItemsWithExcept(index + 1);
    });
    onChangeCardDialogMains(index);
}

function initCardDialogAccordionItemInputs(index) {
    $(`#card-dialog-collapse-i${index} .lg-link-collapse`)
        .off('show.bs.collapse')
        .on('show.bs.collapse', function () {
            onCollapseLgFrame(index);
        });
    $(`#card-dialog-collapse-i${index} .word`)
        .off('input')
        .on('input', function () {
            onChangeCardDialogMains(index);
            insertCardDialogLinks(index);
        });
    $(`#card-dialog-collapse-i${index} .translation`)
        .off('input')
        .on('input', function () {
            onChangeCardDialogMains(index);
        });
    $(`#card-dialog-collapse-i${index} .primary-word`)
        .off('change')
        .on('change', function () {
            onChangeCardDialogMains(index);
        });
    const select = $(`#card-dialog-collapse-i${index} .part-of-speech`)
        .html('').append($(`<option value="-1"></option>`));
    $.each(selectedDictionary.partsOfSpeech, function (i, value) {
        select.append($(`<option value="${i}">${value}</option>`));
    });
}

function closeAccordionItemsWithExcept(index) {
    $('#card-dialog .accordion-button').each(function () {
        if ($(this).attr('data-bs-target').includes(`-i${index}`)) {
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
    const soundBtn = $(`#card-dialog-collapse-i${index} .sound`);
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
    $(`#${actionId}-card-prompt-confirm`).off('click').on('click', function () {
        const body = $(`#${actionId}-card-prompt-body`);
        const id = body.attr('item-id');
        if (!id) {
            return;
        }
        const onDone = function () {
            drawDictionaryCardsPage();
            if (actionId !== 'delete') {
                scrollToRow(`#w${id}`, '#words-table-row', markRowSelected);
            }
        };
        if (actionId === 'delete') {
            deleteCard(id, onDone);
        } else {
            resetCard(id, onDone);
        }
    });
}

/**
 * Updates the state of elements within a card dialog based on user input.
 * It handles the enable/disable state of the save button and the visibility of the add word button
 * according to the presence of words and translations in the specified index.
 *
 * @param {number} index - The index of the current card dialog item being modified.
 * Determines logic for enabling/disabling elements.
 * @return {void} This function does not return a value. It performs DOM manipulation on elements within the dialog.
 */
function onChangeCardDialogMains(index) {
    const word = $(`#card-dialog-collapse-i${index} .word`);
    const translation = $(`#card-dialog-collapse-i${index} .translation`);
    const save = $('#card-dialog-save');

    if (index === 1) {
        const checkboxes = $(".row .primary-word").filter(":checked");
        const enabled = word.val() && translation.val() && checkboxes.length === 1;
        save.prop('disabled', !enabled);
    } else {
        const checkboxes = $(".row .primary-word").filter(":checked");
        const enabled = word.val() && checkboxes.length === 1;
        save.prop('disabled', !enabled);
    }

    const addWordButton = $(`#card-dialog-collapse-i${index} .add-word`);
    if (!isLastAccordionItem(index)) {
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

function isLastAccordionItem(index) {
    return $('.accordion-item').find(`#card-dialog-collapse-i${index + 1}`).length === 0;
}

function createResourceCardItem(cards) {
    const itemId = $('#card-dialog-collapse-i1 .word').attr('item-id');

    const resCard = itemId ? jQuery.extend({}, findById(cards, itemId)) : {};
    resCard.dictionaryId = selectedDictionary.dictionaryId;
    resCard.words = [];

    let i = 0
    $('#card-dialog .accordion-item').each(function () {
        const wordInput = $(this).find('.word');
        const transcriptionInput = $(this).find('.transcription');
        const partOfSpeechInput = $(this).find('.part-of-speech option:selected');
        const examplesInput = $(this).find('.examples');
        const translationInput = $(this).find('.translation');
        const primaryWordCheckbox = $(this).find('.primary-word');
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
        w.primary = primaryWordCheckbox.prop('checked') === true;
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

function cleanCardItemMains(index) {
    cleanCardDialogLinks(index);
    $(`#card-dialog-collapse-i${index} .word`).val('');
    $(`#card-dialog-collapse-i${index} .part-of-speech option:selected`).prop('selected', false);
    $(`#card-dialog-collapse-i${index} .transcription`).val('');
    $(`#card-dialog-collapse-i${index} .translation`).val('');
    $(`#card-dialog-collapse-i${index} .examples`).val('');
    $(`#card-dialog-collapse-i${index} .primary-word`).prop('checked', false);
}

function cleanCardDialogLinks(index) {
    $(`#card-dialog-collapse-i${index} .gl-link`).html('');
    $(`#card-dialog-collapse-i${index} .ya-link`).html('');
    $(`#card-dialog-collapse-i${index} .lg-link`).html('');
    $(`#card-dialog-collapse-i${index} .lg-link-collapse`).removeClass('show');
}

function removeExtraAccordionItems() {
    $('#card-dialog .accordion-item').each(function () {
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

function insertCardDialogLinks(index) {
    const input = $(`#card-dialog-collapse-i${index} .word`);
    const sl = selectedDictionary.sourceLang;
    const tl = selectedDictionary.targetLang;
    const text = input.val();
    if (!text) {
        return;
    }
    createLink($(`#card-dialog-collapse-i${index} .gl-link`), toGlURI(text, sl, tl));
    createLink($(`#card-dialog-collapse-i${index} .ya-link`), toYaURI(text, sl, tl));
    createLink($(`#card-dialog-collapse-i${index} .lg-link`), toLgURI(text, sl, tl));
}

function createLink(parent, uri) {
    parent.html(`<a class='btn btn-link' href='${uri}' target='_blank'>${uri}</a>`);
}

function onCollapseLgFrame(index) {
    const sl = selectedDictionary.sourceLang;
    const tl = selectedDictionary.targetLang;

    const lgDiv = $(`#card-dialog-collapse-i${index} .lg-link-collapse div`);
    const dialogLinksDiv = $(`#card-dialog-collapse-i${index} .links`);
    const wordInput = $(`#card-dialog-collapse-i${index} .word`);
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