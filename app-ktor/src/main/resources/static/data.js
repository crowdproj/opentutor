/*!
 * js-library to work with app data (card-resources).
 */

function findById(cards, cardId) {
    return cards.find(e => e.cardId.toString() === cardId.toString());
}

function rememberAnswer(card, stage, booleanAnswer) {
    if (card.currentDetails == null) {
        card.currentDetails = {};
    }
    card.currentDetails[stage] = booleanAnswer;
}

function hasStage(card, stage) {
    return card.currentDetails != null && card.currentDetails[stage] != null;
}

/**
 * Answers true iif the card is fully answered.
 * If there is a wrong answer for any stage, then the method returns false.
 * @param card a data (card)
 * @returns {boolean|undefined}
 */
function isAnsweredRight(card) {
    const details = card.currentDetails;
    if (details == null || !Object.keys(details).length) {
        return undefined;
    }
    for (let key in details) {
        if (!details.hasOwnProperty(key)) {
            continue;
        }
        if (!details[key]) {
            return false;
        }
    }
    return true;
}

/**
 * Answers of array with non-answered items to process.
 * @param cards input array
 * @param limit max length of returned array
 * @returns {*[]} array of items to process
 */
function selectNonAnswered(cards, limit) {
    const res = [];
    for (let i = 0; i < cards.length; i++) {
        let item = cards[i];
        if (item.answered == null || item.answered < numberOfRightAnswers) {
            res.push(item);
        }
        if (limit && res.length === limit) {
            return res;
        }
    }
    return res;
}

/**
 * Represents an array of card-resources as a string, containing only words.
 * @param cards
 * @returns {string}
 */
function toWordString(cards) {
    return cards.map(d => d.word).sort().join(', ');
}

/**
 * Finds translation string from the item that starts with the specified substring ignoring case.
 * @param item - card resource
 * @param test string to test
 * @returns {string} or undefined
 */
function findTranslationStartsWith(item, test) {
    test = test.toLowerCase();
    return toTranslationArray(item).find((s) => s.toLowerCase().startsWith(test));
}

/**
 * Represents an item translations as a single string.
 * @param item - card resource
 * @returns {string}
 */
function toTranslationString(item) {
    return toTranslationArray(item).join(', ');
}

/**
 * Represents an item translations as a flat array.
 * @param item - card resource
 * @returns {array}
 */
function toTranslationArray(item) {
    return $.map(item.translations, function (n) {
        return n;
    });
}

/**
 * Returns learning percentage for card.
 * @param cardItem - card resource
 * @returns {number} - int percentage
 */
function percentage(cardItem) {
    if (cardItem.answered > numberOfRightAnswers) {
        return 100;
    }
    return Math.round(100.0 * cardItem.answered / numberOfRightAnswers);
}

/**
 * Returns an uri to lingvo-online.
 * @param itemWord {string}  - the card resource word
 * @param sourceLang {string}- source language, e.g. "en"
 * @param targetLang {string} - target language, e.g. "en"
 * @returns {string} an uri
 */
function toLgURI(itemWord, sourceLang, targetLang) {
    let fragment = sourceLang.toLowerCase() + '-' + targetLang.toLowerCase() + "/" + encodeURIComponent(itemWord);
    return "https://www.lingvolive.com/en-us/translate/" + fragment;
}

/**
 * Returns an uri to google-translator.
 * @param itemWord {string}  - the card resource word
 * @param sourceLang {string}- source language, e.g. "en"
 * @param targetLang {string} - target language, e.g. "en"
 * @returns {string} an uri
 */
function toGlURI(itemWord, sourceLang, targetLang) {
    let fragment = '?sl=' + sourceLang.toLowerCase() + '&tl=' + targetLang.toLowerCase() +
        "&text=" + encodeURIComponent(itemWord);
    return "https://translate.google.com/" + fragment;
}

/**
 * Returns an uri to yandex-translator.
 * @param itemWord {string}  - the card resource word
 * @param sourceLang {string}- source language, e.g. "en"
 * @param targetLang {string} - target language, e.g. "en"
 * @returns {string} an uri
 */
function toYaURI(itemWord, sourceLang, targetLang) {
    let fragment = '?lang=' + sourceLang.toLowerCase() + '-' + targetLang.toLowerCase() +
        "&text=" + encodeURIComponent(itemWord);
    return "https://translate.yandex.ru/" + fragment;
}

/**
 * Finds first item from the array of cards by the specified prefix.
 * @param array {array} of resource cards
 * @param prefix {string}
 * @returns item
 */
function findItem(array, prefix) {
    if (!prefix.trim()) {
        return null;
    }
    prefix = prefix.trim().toLowerCase();
    return array.find((s) => s.word.toLowerCase().startsWith(prefix));
}