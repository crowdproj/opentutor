/*!
 * tutor (flashcards) js-script library.
 */

const borderDefault = 'border-white';
const borderSelected = 'border-primary';
const borderSuccess = 'border-success';
const borderError = 'border-danger';

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
    const dataLeft = randomArray(data, numberOfWordsPerStage);
    const length = dataLeft.length * numberOfOptionsPerWord;
    getNextCardDeck(dictionary.id, length, function (words) {
        const options = prepareOptionsDataArray(dataLeft, words);
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
        drawWritingCardPage(randomArray(data, numberOfWordsPerStage), 0, () => stageSelfTest());
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
        drawSelfTestCardPage(randomArray(data, numberOfWordsPerStage), 0, () => stageResults());
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
    return selectNonAnswered(data);
}

function drawShowCardPage(data, index, nextStage) {
    if (index >= data.length) { // no more data => display next stage
        nextStage();
        return;
    }
    const page = $('#show');
    const current = data[index];
    const next = index + 1;

    drawAndPlayAudio(page, current.sound);
    displayTitle(page, 'show');
    $('.word', page).html(current.word);
    $('.translations', page).html(toTranslationString(current));
    $('#show-next').unbind('click').on('click', function () {
        drawShowCardPage(data, next, nextStage);
    });
}

function drawMosaicCardPage(data, nextStage) {
    const stage = 'mosaic';

    displayTitle($('#mosaic'), stage);

    const leftPane = $('#mosaic-left');
    const rightPane = $('#mosaic-right');

    const dataLeft = randomArray(data, numberOfWordsPerStage);
    const dataRight = randomArray(data, data.length);

    leftPane.html('');
    dataLeft.forEach(function (value) {
        let left = $(`<div class="card ${borderDefault}" id="${value.id}-left"><h4>${value.word}</h4></div>`);
        left.unbind('click').on('click', function () {
            setDefaultBorder($('#mosaic .card'));
            setBorderClass(left, borderSelected);
            const sound = value.sound;
            if (sound != null) {
                playAudio(sound);
            }
        });
        leftPane.append(left);
    });

    rightPane.html('');
    dataRight.forEach(function (value) {
        let right = $(`<div class="card ${borderDefault}" id="${value.id}-right"><h4>${toTranslationString(value)}</h4></div>`);
        right.unbind('click').on('click', function () {
            const selected = $(`#mosaic-left .${borderSelected}`);
            if (!selected.length || !$('h4', selected).text().trim()) {
                // nothing selected or selected already processed item (with empty text)
                return;
            }
            const rightCards = $('#mosaic-right .card');
            const leftCards = $('#mosaic-left .card');
            setDefaultBorder(rightCards);
            const left = $(document.getElementById(right.attr('id').replace('-right', '-left')));
            if (left.length && !left.text().trim()) { // exists but empty
                return;
            }
            const success = left.is(selected);
            setBorderClass(right, success ? borderSuccess : borderError);
            if (success) {
                left.html('<h4>&nbsp;</h4>').unbind('click');
                right.html('<h4>&nbsp;</h4>').unbind('click');
            }
            const id = selected.attr('id').replace('-left', '');
            const data = findById(dataLeft, id);
            if (!hasStage(data, stage)) {
                // remember only the first answer, the next, even right, will be ignored
                rememberAnswer(data, stage, success);
            }
            if (!leftCards.filter((i, e) => $(e).text().trim()).length) {
                // no more options
                setDefaultBorder(rightCards);
                setDefaultBorder(leftCards);
                setTimeout(() => updateItemsAndCall(dataLeft, stage, nextStage), 500);
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
        updateItemsAndCall(items, stage, nextStage);
        return;
    }

    displayTitle($('#options'), stage);

    const leftPane = $('#options-left');
    const rightPane = $('#options-right');

    const dataLeft = options[index]['left'];
    const dataRight = options[index]['right'];

    let left = $(`<div class="card ${borderDefault}" id="${dataLeft.id}-left"><h4>${dataLeft.word}</h4></div>`);
    left.unbind('click').on('click', function () {
        setBorderClass(left, borderSelected);
        setDefaultBorder($('#options-right .card'));
        const sound = dataLeft.sound;
        if (sound != null) {
            playAudio(sound);
        }
    });
    leftPane.html('').append(left);
    left.click();

    rightPane.html('');
    dataRight.forEach(function (value) {
        let right = $(`<div class="card ${borderDefault}" id="${value.id}-right"><p class="h4">${toTranslationString(value)}</p></div>`);
        right.unbind('click').on('click', function () {
            right.unbind('click');
            setBorderClass(left, borderDefault);
            setDefaultBorder($('#options-right .card'));

            const id = Number.parseInt(right.attr('id').replace('-right', ''));
            const success = dataLeft.id === id;
            if (success) { // strike all other
                $(`#options-right .card:not('#${id}-right')`).each((k, v) => strikeText($(v).find('p')));
            } else {
                strikeText(right.find('p'));
            }
            setBorderClass(right, success ? borderSuccess : borderError);
            if (!hasStage(dataLeft, stage)) {
                // remember only the first answer, the next, even right, will be ignored
                rememberAnswer(dataLeft, stage, success);
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
        updateItemsAndCall(writingData, stage, nextStage);
        return;
    }
    const page = $('#writing');
    const current = writingData[index];

    drawAndPlayAudio(page, current.sound);
    displayTitle(page, stage);
    $('.word', page).html(current.word);

    const clazz = "d-flex justify-content-start p-5 w-100";
    const testDiv = $('#writing-test').show();
    const nextDiv = $('#writing-next').hide();
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
        let suffixText = $(`<h6 class="text-primary">${toTranslationString(current)}</h6>`);
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
        updateItemsAndCall(selfTestData, stage, nextStage);
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

    drawAndPlayAudio(page, current.sound);
    displayTitle(page, stage);
    $('.word', page).html(current.word);
    translation.html(toTranslationString(current));
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
    const right = toWordString(data.filter(d => isAnsweredRight(d)));
    const wrong = toWordString(data.filter(function (d) {
        const res = isAnsweredRight(d);
        return res !== undefined && !res;
    }));
    const learned = toWordString(data.filter(d => d.answered >= numberOfRightAnswers));
    displayTitle(page, 'result');
    $('#result-correct').html(right);
    $('#result-wrong').html(wrong);
    $('#result-learned').html(learned);
    // remove state details
    data.forEach(function (item) {
        delete item.currentDetails
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

function displayTitle(page, stage) {
    $('.card-title', page).html(dictionary.name + ": " + stage);
}

function setDefaultBorder(array) {
    $.each(array, (k, v) => setBorderClass(v, borderDefault));
}

function setBorderClass(item, borderClass) {
    return $(item).attr('class', $(item).attr('class').replace(/\bborder-.+\b/g, borderClass));
}

function strikeText(textHolder) {
    textHolder.html(`<del>${textHolder.text()}</del>`).css('color', 'gray');
}

function drawAndPlayAudio(parent, audio) {
    if (!audio) {
        return;
    }
    const btn = $('.sound', parent);
    btn.prop('disabled', false);
    btn.unbind('click').on('click', function () {
        btn.prop('disabled', true);
        playAudio(audio, function () {
            btn.prop('disabled', false);
        });
    });
    btn.click();
}

function updateItemsAndCall(array, stage, nextStageCallback) {
    patchCard(toUpdateResource(array, stage), function () {
        updateItemResource(array, stage);
        nextStageCallback();
    });
}