/*!
 * tutor (flashcards) js-script library.
 */

/**
 * an array with card resources.
 * card:
 * ```json
 * {
 *  "cardId": "...",
 *  "dictionaryId": "...",
 *  "dictionaryName": "...",
 *  "words": [
 *    {
 *      "word": "...",
 *      "transcription": "...",
 *      "partOfSpeech": "...",
 *      "translations": [
 *        [ "..." ]
 *      ],
 *      "examples": [
 *        {
 *          "example": "...",
 *          "translation": "..."
 *        }
 *      ]
 *    }
 *  ],
 *  "stats": {},
 *  "details": {},
 *  "answered": int,
 *  "wrong": boolean,
 *  "sound": bytes
 * }
 * ```
 */
let flashcards;
/**
 * ```json
 * {
 *  "stageShowNumberOfWords": int,
 *  "stageOptionsNumberOfVariants": int,
 *  "numberOfWordsPerStage": int
 * }
 * ```
 */
let settings;

const borderWhite = 'border-white';
const borderPrimary = 'border-primary';
const borderSuccess = 'border-success';
const borderLight = 'border-light';
const borderDanger = 'border-danger';

/**
 * First stage: show.
 */
function stageShow() {
    const data = selectNextCardsDeck();
    if (data.length > 0) {
        displayPage('show');
        drawShowCardPage(data, 0, () => stageMosaic());
        return;
    }
    stageResults();
}

/**
 * Second stage: mosaic.
 */
function stageMosaic() {
    const data = selectNextCardsDeck();
    if (data.length > 0) {
        displayPage('mosaic');
        drawMosaicCardPage(data, () => stageOptions());
        return;
    }
    stageResults();
}

/**
 * Third stage: options.
 */
function stageOptions() {
    const data = selectNextCardsDeck();
    if (data.length === 0) {
        stageResults();
        return;
    }
    const dataLeft = randomArray(data, settings.numberOfWordsPerStage);
    const length = dataLeft.length * settings.stageOptionsNumberOfVariants;

    const dictionaryIds = dataLeft.map(it => it.dictionaryId);
    getNextCardDeck(dictionaryIds, length, false, function (cards) {
        const options = prepareOptionsDataArray(dataLeft, cards);
        displayPage('options');
        drawOptionsCardPage(options, 0, () => stageWriting());
    });
}

/**
 * Fourth stage: writing.
 */
function stageWriting() {
    const data = selectNextCardsDeck();
    if (data.length > 0) {
        displayPage('writing');
        drawWritingCardPage(randomArray(data, settings.numberOfWordsPerStage), 0, () => stageSelfTest());
        return;
    }
    stageResults();
}

/**
 * Fifth stage: self-test.
 */
function stageSelfTest() {
    const data = selectNextCardsDeck();
    if (data.length > 0) {
        displayPage('self-test');
        drawSelfTestCardPage(randomArray(data, settings.numberOfWordsPerStage), 0, () => stageResults());
        return;
    }
    stageResults();
}

/**
 * The last "stage": show results.
 */
function stageResults() {
    displayPage('result');
    drawResultCardPage();
}

/**
 * Returns a deck of cards to process.
 * @returns {*[]} array of card resources
 */
function selectNextCardsDeck() {
    return selectNonAnswered(flashcards, null);
}

function drawShowCardPage(cards, index, nextStage) {
    if (index >= cards.length) { // no more data => display next stage
        nextStage();
        return;
    }
    const page = $('#show');
    const current = cards[index];

    const status = '(' + percentage(current, null) + '%) ';

    drawAndPlayAudio(page, current.sound);
    displayTitle(page, 'show: ' + current.dictionaryName);
    $('.word', page).html(getAllWordsAsString(current));
    $('.translations', page).html(getTranslationsAsHtml(current));
    $('.examples', page).html(getExamplesAsHtml(current));
    $('.status', page).html(status);

    $('#show-next').unbind('click').on('click', function () {
        drawShowCardPage(cards, index + 1, nextStage);
    });

    $('#know').unbind('click').on('click', function () {
        current.answered = current.numberOfRightAnswers
        updateCard(current, function () {
            cards.splice(index, 1);
            drawShowCardPage(cards, index, nextStage);
        })
    });
}

function drawMosaicCardPage(cards, nextStage) {
    const stage = 'mosaic';

    displayTitle($('#mosaic'), stage);

    const leftPane = $('#mosaic-left');
    const rightPane = $('#mosaic-right');

    const dataLeft = randomArray(cards, settings.numberOfWordsPerStage);
    const dataRight = randomArray(cards, cards.length);

    leftPane.html('');
    dataLeft.forEach(function (card) {
        let left = $(`<div class="card ${borderWhite}" id="${card.cardId}-left"><h4>${getAllWordsAsString(card)}</h4></div>`);
        left.unbind('click').on('click', function () {
            const rightCards = $('#mosaic-right .card');
            const leftCards = $('#mosaic-left .card');
            setBorders(rightCards, borderLight);
            setBorders(leftCards, borderWhite);
            setBorderClass(left, borderPrimary);
            const sound = card.sound;
            if (sound != null) {
                playAudio(sound);
            }
        });
        leftPane.append(left);
    });

    rightPane.html('');
    dataRight.forEach(function (value) {
        let right = $(`<div class="card ${borderLight}" id="${value.cardId}-right"><h4>${getTranslationsAsString(value)}</h4></div>`);
        right.unbind('click').on('click', function () {
            const selected = $(`#mosaic-left .${borderPrimary}`);
            if (!selected.length || !$('h4', selected).text().trim()) {
                // nothing selected or selected already processed item (with empty text)
                return;
            }
            const rightCards = $('#mosaic-right .card');
            const leftCards = $('#mosaic-left .card');
            setBorders(rightCards, borderLight);
            const left = $(document.getElementById(right.attr('id').replace('-right', '-left')));
            if (left.length && !left.text().trim()) { // exists but empty
                return;
            }
            const success = left.is(selected);
            setBorderClass(right, success ? borderSuccess : borderDanger);
            if (success) {
                left.html('<h4>&nbsp;</h4>').unbind('click');
                right.html('<h4>&nbsp;</h4>').unbind('click');
            }
            const id = selected.attr('id').replace('-left', '');
            const cardResource = findById(dataLeft, id);
            if (success) {
                if (!hasStage(cardResource, stage)) {
                    // remember only the first answer, the next, even right, will be ignored
                    rememberAnswer(cardResource, stage, true);
                }
            } else {
                rememberAnswer(cardResource, stage, false);
            }
            if (!leftCards.filter((i, e) => $(e).text().trim()).length) {
                // no more options
                setBorders(rightCards, borderLight);
                setBorders(leftCards, borderWhite);
                setTimeout(() => updateCardAndCallNext(dataLeft, nextStage), 500);
            }
        });
        rightPane.append(right);
    });
}

function drawOptionsCardPage(options, index, nextStage) {
    const stage = 'options';
    if (index >= options.length) { // no more data => display next stage
        const items = options.map(function (item) {
            return item['left'];
        });
        updateCardAndCallNext(items, nextStage);
        return;
    }

    const leftPane = $('#options-left');
    const rightPane = $('#options-right');

    const dataLeft = options[index]['left'];
    const dataRight = options[index]['right'];

    displayTitle($('#options'), stage + ": " + dataLeft.dictionaryName);

    let left = $(`<div class="card ${borderWhite}" id="${dataLeft.cardId}-left"><h4>${getAllWordsAsString(dataLeft)}</h4></div>`);
    left.unbind('click').on('click', function () {
        setBorderClass(left, borderPrimary);
        setBorders($('#options-right .card'), borderWhite);
        const sound = dataLeft.sound;
        if (sound != null) {
            playAudio(sound);
        }
    });
    leftPane.html('').append(left);
    left.click();

    rightPane.html('');
    dataRight.forEach(function (value) {
        let right = $(`<div class="card ${borderLight}" id="${value.cardId}-right"><p class="h4">${getTranslationsAsString(value)}</p></div>`);
        right.unbind('click').on('click', function () {
            right.unbind('click');
            setBorderClass(left, borderWhite);
            setBorders($('#options-right .card'), borderLight);

            const id = Number.parseInt(right.attr('id').replace('-right', '')).toString();
            const success = dataLeft.cardId === id;
            if (success) { // strike all others
                const selector = '#' + id + '-right'
                $(`#options-right .card:not('${selector}')`).each((k, v) => strikeText($(v).find('p')));
            } else {
                strikeText(right.find('p'));
            }
            setBorderClass(right, success ? borderSuccess : borderDanger);
            if (success) {
                if (!hasStage(dataLeft, stage)) {
                    // remember only the first answer, the next, even right, will be ignored
                    rememberAnswer(dataLeft, stage, true);
                }
            } else {
                rememberAnswer(dataLeft, stage, false);
            }
            if (success) {
                // go to next
                setTimeout(() => drawOptionsCardPage(options, index + 1, nextStage), 500);
            }
        });
        rightPane.append(right);
    });
}

function drawWritingCardPage(writingData, index, nextStage) {
    const stage = 'writing';
    if (index >= writingData.length) {
        updateCardAndCallNext(writingData, nextStage);
        return;
    }
    const page = $('#writing');
    const current = writingData[index];

    const status = '(' + percentage(current, null) + '%) ';

    drawAndPlayAudio(page, current.sound);
    displayTitle(page, stage + ': ' + current.dictionaryName);
    $('.word', page).html(getAllWordsAsString(current));

    const clazz = "d-flex justify-content-start p-5 w-100";
    const testDiv = $('#writing-test').show();
    const nextDiv = $('#writing-next').hide();
    $('.status', page).html(status);
    const textareaInput = $(`<input type="text" class="${clazz}"/>`);
    const textareaRow = $('#writing-textarea').html('').append(textareaInput);

    const testButton = $('button', testDiv).prop("disabled", true);
    const nextButton = $('button', nextDiv);

    textareaInput.val('').on('keyup', function () {
        testButton.prop("disabled", textareaInput.val().length === 0);
    });
    testButton.unbind('click').on('click', function () {
        let res;
        const givenAnswer = textareaInput.val();
        const rightAnswer = findTranslationStartsWith(current, givenAnswer);
        let prefixText;
        if (rightAnswer) {
            res = true;
            prefixText = $(`<h6 class="text-success">${rightAnswer}</h6>`);
        } else {
            res = false;
            prefixText = $(`<del><h6 class="text-danger">${givenAnswer}</h6></del>`);
        }
        let suffixText = $(`<h6 class="text-primary">${getTranslationsAsString(current)}</h6>`);
        const translationsDiv = $(`<div class="${clazz}"></div>`)
            .append(prefixText).append(`<h6>&nbsp;&#8212;&nbsp;</h6>`)
            .append(suffixText);
        textareaRow.html('').append(translationsDiv);
        testDiv.hide();
        nextDiv.show();
        rememberAnswer(current, stage, res);
    });
    nextButton.unbind('click').on('click', function () {
        // go to next
        drawWritingCardPage(writingData, index + 1, nextStage);
    });
}

function drawSelfTestCardPage(selfTestData, index, nextStage) {
    const stage = 'self-test';
    if (index >= selfTestData.length) {
        updateCardAndCallNext(selfTestData, nextStage);
        return;
    }
    const page = $('#self-test');

    const translation = $('.translations', page);
    const curtain = $('#self-test-display-translation');
    const display = $('#self-test-display-translation button');
    const correct = $('#self-test-correct');
    const wrong = $('#self-test-wrong');

    const current = selfTestData[index];
    const next = index + 1;

    const status = '(' + percentage(current, null) + '%) ';

    drawAndPlayAudio(page, current.sound);
    displayTitle(page, stage + ': ' + current.dictionaryName);
    $('.word', page).html(getAllWordsAsString(current));
    $('.status', page).html(status);
    translation.html(getTranslationsAsString(current));
    correct.prop('disabled', true);
    wrong.prop('disabled', true);
    translation.hide();
    curtain.show();

    display.unbind('click').on('click', function () {
        display.unbind('click');
        curtain.hide();
        translation.show();
        correct.prop('disabled', false);
        wrong.prop('disabled', false);
    });
    correct.unbind('click').on('click', function () {
        correct.unbind('click');
        rememberAnswer(current, stage, true);
        drawSelfTestCardPage(selfTestData, next, nextStage);
    });
    wrong.unbind('click').on('click', function () {
        wrong.unbind('click');
        rememberAnswer(current, stage, false);
        drawSelfTestCardPage(selfTestData, next, nextStage);
    });
}

function drawResultCardPage() {
    const page = $('#result');
    const tbody = $('#result tbody');
    const wrong = flashcards.filter(function (card) {
        const res = isAnsweredRight(card);
        return res !== undefined && !res;
    }).sort((a, b) => b.answered - a.answered);
    const learned = flashcards
        .filter(card => card.answered >= card.numberOfRightAnswers);
    const correct = flashcards.filter(card => isAnsweredRight(card))
        .filter(item => !learned.includes(item))
        .sort((a, b) => b.answered - a.answered);

    displayTitle(page, 'result');
    learned.forEach(function (card) {
        const row = $(`<tr id="${'w' + card.cardId}">
                            <td class="text-success"><b>${getAllWordsAsString(card)}</b></td>
                            <td>${getAllTranslationsAsString(card)}</td>
                            <td>${card.dictionaryName}</td>
                            <td class="text-success"><b>${percentage(card, null)}</b></td>
                          </tr>`);
        tbody.append(row);
    });
    correct.forEach(function (card) {
        const row = $(`<tr id="${'w' + card.cardId}">
                            <td class="text-primary"><b>${getAllWordsAsString(card)}</b></td>
                            <td>${getAllTranslationsAsString(card)}</td>
                            <td>${card.dictionaryName}</td>
                            <td class="text-primary"><b>${percentage(card, null)}</b></td>
                          </tr>`);
        tbody.append(row);
    });
    wrong.forEach(function (card) {
        const row = $(`<tr id="${'w' + card.cardId}">
                            <td class="text-danger"><b>${getAllWordsAsString(card)}</b></td>
                            <td>${getAllTranslationsAsString(card)}</td>
                            <td>${card.dictionaryName}</td>
                            <td class="text-danger"><b>${percentage(card, null)}</b></td>
                          </tr>`);
        tbody.append(row);
    });
    // remove state details
    flashcards.forEach(function (item) {
        delete item.stageStats
    });
}

function prepareOptionsDataArray(dataLeft, dataRight) {
    const numOptionsPerCard = dataRight.length / dataLeft.length;
    const res = [];
    for (let i = 0; i < dataLeft.length; i++) {
        const left = dataLeft[i];
        let right = dataRight.slice(i * numOptionsPerCard, (i + 1) * numOptionsPerCard);
        right.push(left);
        right = removeDuplicates(right);
        shuffleArray(right);

        const item = {};
        item['left'] = left;
        item['right'] = right;
        res.push(item);
    }
    return res;
}

function displayTitle(page, title) {
    $('.card-title', page).html(title);
}

function setBorders(array, clazz) {
    $.each(array, (k, v) => setBorderClass(v, clazz));
}

function setBorderClass(item, borderClass) {
    return $(item).attr('class', $(item).attr('class').replace(/\bborder-.+\b/g, borderClass));
}

function strikeText(textHolder) {
    textHolder.html(`<del>${textHolder.text()}</del>`).css('color', 'gray');
}

function drawAndPlayAudio(parent, audio) {
    const btn = $('.sound', parent);
    btn.unbind('click');
    if (!audio) {
        btn.prop('disabled', true);
        return;
    }
    btn.prop('disabled', false);
    btn.on('click', function () {
        btn.prop('disabled', true);
        playAudio(audio, function () {
            btn.prop('disabled', false);
        });
    });
    btn.click();
}

function updateCardAndCallNext(cards, nextStageCallback) {
    const res = [];
    cards.forEach(function (card) {
        if (card.answered === undefined) {
            card.answered = 0;
        }
        if (card.sessionStats === undefined) {
            card.sessionStats = {};
        }
        card.answered += sumAnswers(card)
        if (card.answered < 0) {
            card.answered = 0;
        }
        if (card.wrong && card.answered >= card.numberOfRightAnswers) {
            card.answered = card.numberOfRightAnswers - 1;
        }
        const learn = {};
        learn['cardId'] = card.cardId;
        learn['details'] = card.stageStats;
        res.push(learn);
        card.sessionStats = {...card.sessionStats, ...card.stageStats};
        card.stageStats = {};
    })
    learnCard(res, () => nextStageCallback());
}