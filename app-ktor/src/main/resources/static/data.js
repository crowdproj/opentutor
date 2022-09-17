/*!
 * js-library to work with app data (card-resources).
 */

function findById(array, id) {
    return array.find(e => e.id.toString() === id.toString());
}

function rememberAnswer(item, stage, booleanAnswer) {
    if (item.currentDetails == null) {
        item.currentDetails = {};
    }
    item.currentDetails[stage] = booleanAnswer;
}

function hasStage(item, stage) {
    return item.currentDetails != null && item.currentDetails[stage] != null;
}

/**
 * Answers true iif the card is fully answered.
 * If there is a wrong answer for any stage, then the method returns false.
 * @param item a data (card)
 * @returns {boolean|undefined}
 */
function isAnsweredRight(item) {
    const details = item.currentDetails;
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
 * Answers a resource for sending to server.
 *
 * @param array a data array (items)
 * @param stage i.e. 'self-test', 'mosaic'
 * @returns {string}
 */
function toUpdateResource(array, stage) {
    const copyToUpdateResource = function (st, updateResource, itemResource) {
        updateResource.details = {};
        updateResource.details[st] = itemResource.currentDetails[st] ? 1 : 0; // boolean to int
    }
    const res = array.map(function (item) {
        const cardUpdateResource = {};
        cardUpdateResource.id = item.id;
        if (stage) {
            copyToUpdateResource(stage, cardUpdateResource, item);
        } else {
            // copy all
            for (let s in item.currentDetails) {
                if (!item.currentDetails.hasOwnProperty(s)) {
                    continue;
                }
                copyToUpdateResource(s, cardUpdateResource, item);
            }
        }
        return cardUpdateResource;
    });
    return JSON.stringify(res);
}

/**
 * Updates the array of items-resources by coping item.currentDetails to item.details.
 * @param array a data array (items)
 * @param stage i.e. 'self-test', 'mosaic'
 */
function updateItemResource(array, stage) {
    const updateItem = function (st, itemResource) {

        if (itemResource.details == null) {
            itemResource.details = {};
        }
        let value;
        if (itemResource.details[st]) {
            value = itemResource.details[st];
        } else {
            value = 1;
        }
        value = value << 1;
        if (itemResource.currentDetails[st]) {
            value = value + 1;
            itemResource.answered++;
        }
        itemResource.details[st] = value;
    }
    array.forEach(function (item) {
        if (stage) {
            updateItem(stage, item);
        } else {
            for (let s in item.currentDetails) {
                if (!item.currentDetails.hasOwnProperty(s)) {
                    continue;
                }
                updateItem(s, item);
            }
        }
    });
}

/**
 * Answers of array with non-answered items to process.
 * @param array input array
 * @param limit max length of returned array
 * @returns {*[]} array of items to process
 */
function selectNonAnswered(array, limit) {
    const res = [];
    for (let i = 0; i < array.length; i++) {
        let item = array[i];
        if (item.answered < numberOfRightAnswers) {
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
 * @param array
 * @returns {string}
 */
function toWordString(array) {
    return array.map(d => d.word).sort().join(', ');
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
 * @param item - card resource
 * @returns {number} - int percentage
 */
function percentage(item) {
    if (item.answered > numberOfRightAnswers) {
        return 100;
    }
    return Math.round(100.0 * item.answered / numberOfRightAnswers);
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