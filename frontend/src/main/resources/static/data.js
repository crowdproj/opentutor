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
    return cards.find(card => card.cardId.toString() === cardId.toString())
}

function rememberAnswer(card, stage, booleanAnswer) {
    if (card.stageStats == null) {
        card.stageStats = {}
    }
    card.stageStats[stage] = booleanAnswer ? 1 : -1
}

function hasStage(card, stage) {
    return card.stageStats != null && card.stageStats[stage] != null
}

/**
 * Answers true if the card is fully answered.
 * If there is a wrong answer for any stage, then the method returns false.
 * @param card a data (card)
 * @returns {boolean|undefined}
 */
function isAnsweredRight(card) {
    const details = card.sessionStats
    if (details == null || !Object.keys(details).length) {
        return undefined
    }
    for (let key in details) {
        if (!details.hasOwnProperty(key)) {
            continue
        }
        if (details[key] !== 1) {
            return false
        }
    }
    return true
}

/**
 * Sums all answers to get a number to add to `card.answered`.
 * @param card
 * @returns {number}
 */
function sumAnswers(card) {
    const details = card.stageStats
    if (details == null || !Object.keys(details).length) {
        return 0
    }
    let res = 0
    for (let key in details) {
        if (!details.hasOwnProperty(key)) {
            continue
        }
        if (details[key]) {
            res += 1
        } else {
            res -= 1
        }
    }
    return res
}

/**
 * Answers of an array with non-answered items to process.
 * @param cards input array
 * @param limit max length of returned array
 * @returns {*[]} array of items to process
 */
function selectNonAnswered(cards, limit) {
    const res = []
    for (let i = 0; i < cards.length; i++) {
        let card = cards[i]
        if (card.answered == null || card.answered < numberOfRightAnswers) {
            res.push(card)
        }
        if (limit && res.length === limit) {
            return res
        }
    }
    return res
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @returns {*}
 */
function getCardFirstWordWord(card) {
    return card.words[0].word
}

/**
 * @param card
 * @returns {*}
 */
function getAllWordsAsString(card) {
    return card.words.map(it => it.word).join(', ')
}

/**
 * Represents an item translations as a single string.
 * @param card - card resource
 * @returns {string}
 */
function getAllTranslationsAsString(card) {
    let arrayOfArrays = $.map(card.words.map(it => it.translations), function (n) {
        return n
    })
    return $.each(arrayOfArrays, function (n) {
        return n
    }).join(', ')
}

/**
 * Finds translation string from the item that starts with the specified substring ignoring case.
 * **[TODO] For first word only.**
 * @param card - card resource
 * @param test string to test
 * @returns {string} or undefined
 */
function findTranslationStartsWith(card, test) {
    test = test.toLowerCase()
    return getCardFirstWordTranslationsAsArray(card).find((s) => s.toLowerCase().startsWith(test))
}

/**
 * Represents an item translations as a single string.
 * **[TODO] For first word only.**
 * @param card - card resource
 * @returns {string}
 */
function getTranslationsAsString(card) {
    return getCardFirstWordTranslationsAsArray(card).join(', ')
}

function getTranslationsAsHtml(card) {
    return card.words
        .map(word => {
            return getWordTranslationsAsArray(word)
        })
        .filter(item => {
            return 0 < item.length
        })
        .map(item => item.join(", "))
        .join("<br>")
}

/**
 * Represents an item translations as a flat array.
 * **[TODO] For first word only.**
 * @param card - card resource
 * @returns {array}
 */
function getCardFirstWordTranslationsAsArray(card) {
    // first word
    return getWordTranslationsAsArray(card.words[0])
}

/**
 *
 * @param word
 * @returns {*}
 */
function getWordTranslationsAsArray(word) {
    return $.map(word.translations, function (n) {
        return n
    })
}

function getExamplesAsHtml(card) {
    return card.words
        .map(word => {
            return getWordExamplesAsArray(word)
        })
        .filter(item => {
            return 0 < item.length
        })
        .map(item => item.join(", "))
        .join("<br>")
}

function getWordExamplesAsArray(word) {
    return word.examples.map(ex => {
        const suffix = ex.translation != null ? ` (${ex.translation})` : "";
        return ex.example + suffix
    })
}

/**
 * Returns learning percentage for card.
 * @param cardItem - card resource
 * @returns {number} - int percentage
 */
function percentage(cardItem) {
    if (!cardItem.answered) {
        return 0
    }
    if (cardItem.answered > numberOfRightAnswers) {
        return 100
    }
    return Math.round(100.0 * cardItem.answered / numberOfRightAnswers)
}

/**
 * Returns an uri to lingvo-online.
 * @param itemWord {string}  - the card resource word
 * @param sourceLang {string}- source language, e.g. "en"
 * @param targetLang {string} - target language, e.g. "en"
 * @returns {string} an uri
 */
function toLgURI(itemWord, sourceLang, targetLang) {
    let fragment = sourceLang.toLowerCase() + '-' + targetLang.toLowerCase() + "/" + encodeURIComponent(itemWord)
    return "https://www.lingvolive.com/en-us/translate/" + fragment
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
        "&text=" + encodeURIComponent(itemWord)
    return "https://translate.google.com/" + fragment
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
        "&text=" + encodeURIComponent(itemWord)
    return "https://translate.yandex.ru/" + fragment
}

function getLanguageNameByCode(code) {
    const language = languages.find(lang => lang.code === code);
    return language ? language.name : code;
}