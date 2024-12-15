/*!
 * js-library to work with app data (card-resources).
 *
 * CARD:
 * ```js
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
 *  "details": {}
 * }
 * ```
 * DICTIONARY:
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

function findById(cards, cardId) {
    return cards.find(card => card.cardId.toString() === cardId.toString());
}

function rememberAnswer(card, stage, booleanAnswer) {
    if (card.stageStats == null) {
        card.stageStats = {};
    }
    if (!booleanAnswer) {
        card.wrong = true;
    }
    card.stageStats[stage] = booleanAnswer ? 1 : -1;
}

function hasStage(card, stage) {
    return card.stageStats != null && card.stageStats[stage] != null;
}

/**
 * Answers true if the card is fully answered.
 * If there is a wrong answer for any stage, then the method returns false.
 * @param card a data (card)
 * @returns {boolean|undefined}
 */
function isAnsweredRight(card) {
    const details = card.sessionStats;
    if (details == null || !Object.keys(details).length) {
        return undefined;
    }
    for (let key in details) {
        if (!details.hasOwnProperty(key)) {
            continue;
        }
        if (details[key] !== 1) {
            return false;
        }
    }
    return true;
}

/**
 * Sums all answers to get a number to add to `card.answered`.
 * @param card
 * @returns {number}
 */
function sumAnswers(card) {
    const details = card.stageStats;
    if (details == null || !Object.keys(details).length) {
        return 0;
    }
    let res = 0;
    for (let key in details) {
        if (!details.hasOwnProperty(key)) {
            continue
        }
        if (details[key]) {
            res += 1;
        } else {
            res -= 1;
        }
    }
    return res;
}

/**
 * Answers of an array with non-answered items to process.
 * @param cards input array
 * @param limit max length of returned array
 * @returns {*[]} array of items to process
 */
function selectNonAnswered(cards, limit) {
    const res = [];
    for (let i = 0; i < cards.length; i++) {
        let card = cards[i];
        if (card.answered == null || card.answered < card.numberOfRightAnswers) {
            res.push(card);
        }
        if (limit && res.length === limit) {
            return res;
        }
    }
    return res;
}

/**
 * Gets primary words as string.
 * @param card
 * @returns {*}
 */
function getCardWord(card) {
    return getPrimaryCardWord(card).word;
}

/**
 * Finds translation string from the item that starts with the specified substring ignoring case.
 * @param card - card resource
 * @param test string to test
 * @returns {string} or undefined
 */
function findTranslationStartsWith(card, test) {
    test = test.toLowerCase();
    return getCardPrimaryWordTranslationsAsArray(card).find((s) => s.toLowerCase().startsWith(test));
}

/**
 * Returns first word if it matches the given test string.
 * **[TODO] For first word only.**
 * @param card - card resource
 * @param test string to test
 * @returns {string} or undefined
 */
function findWordStartsWith(card, test) {
    test = test.toLowerCase();
    const s = card.words[0].word.toLowerCase();
    if (s.startsWith(test)) {
        return card.words[0].word;
    }
    return undefined;
}

/**
 * Searches through a list of card objects to find the first card
 * whose words, when concatenated as a string, start with the specified prefix.
 *
 * @param {Array} cards - An array of card objects to search through.
 * @param {string} prefix - The prefix to match at the start of the concatenated word string.
 * @return {object|null} The first card object that matches the prefix, or null if no match is found or the prefix is invalid.
 */
function findCardByWordPrefix(cards, prefix) {
    if (prefix === null || prefix === undefined) {
        return null;
    }
    const search = prefix.trim().toLowerCase();
    if (search.length === 0) {
        return null;
    }
    return cards.find((card) => getCardWord(card).toLowerCase().startsWith(search));
}

/**
 * Represents an item translations as a single string.
 * @param card - card resource
 * @returns {string}
 */
function getTranslationsAsString(card) {
    return getCardPrimaryWordTranslationsAsArray(card).join(', ');
}

function getTranslationsAsHtml(card) {
    return getPrimaryCardWord(card).translations.map(item => item.join(", ")).join("<br>");
}

/**
 * Represents an item translations as a flat array.
 * **[TODO] For first word only.**
 * @param card - card resource
 * @returns {array}
 */
function getCardPrimaryWordTranslationsAsArray(card) {
    return getWordTranslationsAsArray(getPrimaryCardWord(card));
}

/**
 *
 * @param word
 * @returns {*}
 */
function getWordTranslationsAsArray(word) {
    return $.map(word.translations, function (n) {
        return n;
    })
}

function getExamplesAsHtml(card) {
    return card.words
        .map(word => {
            return getWordExamplesAsArray(word)
        })
        .filter(item => {
            return 0 < item.length;
        })
        .map(item => item.join(", "))
        .join("<br>");
}

function getWordExamplesAsArray(word) {
    return word.examples.map(ex => {
        const suffix = ex.translation != null ? ` (${ex.translation})` : "";
        return ex.example + suffix;
    })
}

function getCardSound(card) {
    return getPrimaryCardWord(card).sound;
}

function getPrimaryCardWord(card) {
    const res = card.words.filter(word => word.primary === true);
    if (res.length === 1) {
        return res[0];
    } else {
        throw new Error("No single primary word found, card: " + card.cardId);
    }
}

/**
 * Returns learning percentage for card.
 * @param cardItem - card resource
 * @param numberOfRightAnswers or null
 * @returns {number} - int percentage
 */
function percentage(cardItem, numberOfRightAnswers) {
    if (!cardItem.answered) {
        return 0;
    }
    let nora = cardItem.numberOfRightAnswers;
    if (!nora && numberOfRightAnswers !== null) {
        nora = numberOfRightAnswers;
    }
    if (!nora) {
        throw Error("No numberOfRightAnswers");
    }
    if (cardItem.answered > nora) {
        return 100;
    }
    return Math.round(100.0 * cardItem.answered / nora);
}

/**
 * Returns an uri to lingvo-online.
 * @param itemWord {string}  - the card resource word
 * @param sourceLang {string}- source language, e.g. "en"
 * @param targetLang {string} - target language, e.g. "en"
 * @returns {string} an uri
 */
function toLgURI(itemWord, sourceLang, targetLang) {
    const fragment = sourceLang.toLowerCase() + '-' + targetLang.toLowerCase() + "/" + encodeURIComponent(itemWord);
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

function getLanguageNameByCode(code) {
    const language = languages.find(lang => lang.code === code);
    return language ? language.name : code;
}

/**
 * Removes duplicated cards.
 * @param cards
 * @returns {*[]} - a new array
 */
function removeDuplicates(cards) {
    const res = [];
    const ids = new Set();

    $.each(cards, function (i, e) {
        if (!ids.has(e.cardId)) {
            ids.add(e.cardId);
            res.push(e);
        }
    });
    return res;
}

function isEmptyCard(card) {
    return card.words.length === 0;
}