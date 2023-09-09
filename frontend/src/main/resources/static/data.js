/*!
 * js-library to work with app data (card-resources).
 *
 * card:
 * ```js
 * {
 *  "cardId": "...",
 *  "dictionaryId": "...",
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
 *
*/

function findById(cards, cardId) {
    return cards.find(card => card.cardId.toString() === cardId.toString())
}

function rememberAnswer(card, stage, booleanAnswer) {
    if (card.currentDetails == null) {
        card.currentDetails = {}
    }
    card.currentDetails[stage] = booleanAnswer
}

function hasStage(card, stage) {
    return card.currentDetails != null && card.currentDetails[stage] != null
}

/**
 * Answers true if the card is fully answered.
 * If there is a wrong answer for any stage, then the method returns false.
 * @param card a data (card)
 * @returns {boolean|undefined}
 */
function isAnsweredRight(card) {
    const details = card.currentDetails
    if (details == null || !Object.keys(details).length) {
        return undefined
    }
    for (let key in details) {
        if (!details.hasOwnProperty(key)) {
            continue
        }
        if (!details[key]) {
            return false
        }
    }
    return true
}

/**
 * Answers of array with non-answered items to process.
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
 * **[TODO] For first word only.**
 * @param card
 * @returns {*}
 */
function getCardFirstWordTranscriptionAsArrayArray(card) {
    return card.words[0].transcription
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @returns {Array}
 */
function getCardFirstWordExamplesAsArray(card) {
    return card.words[0].examples.map(it => it.example)
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @returns {*}
 */
function getCardFirstWordTranslationsAsArrayArray(card) {
    return card.words[0].translations
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @returns {*}
 */
function getCardFirstWordPartOfSpeech(card) {
    return card.words[0].partOfSpeech
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @returns {*}
 */
function getCardFirstWordSound(card) {
    return card.words[0].sound
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @param word
 */
function setCardFirstWordWord(card, word) {
    if (card.words == null) {
        card.words = []
    }
    if (card.words.length === 0) {
        card.words.push({})
    }
    card.words[0].word = word
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @param transcription
 */
function setCardFirstWordTranscription(card, transcription) {
    if (card.words == null) {
        card.words = []
    }
    if (card.words.length === 0) {
        card.words.push({})
    }
    card.words[0].transcription = transcription
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @param pos
 */
function setCardFirstWordPartOfSpeech(card, pos) {
    if (card.words == null) {
        card.words = []
    }
    if (card.words.length === 0) {
        card.words.push({})
    }
    card.words[0].partOfSpeech = pos
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @param examples
 */
function setCardFirstWordExamplesArray(card, examples) {
    if (card.words == null) {
        card.words = []
    }
    if (card.words.length === 0) {
        card.words.push({})
    }
    card.words[0].examples = []
    examples.forEach(function(example) {
        let ex = { example: example }
        card.words[0].examples.push(ex)
    })
}

/**
 * **[TODO] For first word only.**
 * @param card
 * @param translations
 */
function setCardFirstWordTranslationsArrayArray(card, translations) {
    if (card.words == null) {
        card.words = []
    }
    if (card.words.length === 0) {
        card.words.push({})
    }
    card.words[0].translations = translations
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
    let arrayOfArrays =  $.map(card.words.map(it => it.translations), function (n) {
        return n
    })
    return $.each(arrayOfArrays, function (n) {
        return n
    }).join(', ')
}

/**
 * Represents an array of card-resources as a string, containing only words.
 * **[TODO] For first word only.**
 * For report stage.
 * @param cards
 * @returns {string}
 */
function getCardsWordsAsString(cards) {
    return cards.map(it => it.words[0]).map(it => it.word).sort().join(', ')
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

/**
 * Returns learning percentage for card.
 * @param cardItem - card resource
 * @returns {number} - int percentage
 */
function percentage(cardItem) {
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

/**
 * Finds first item from the array of cards by the specified prefix.
 * @param array {array} of resource cards
 * @param prefix {string}
 * @returns item
 */
function findItem(array, prefix) {
    if (!prefix.trim()) {
        return null
    }
    prefix = prefix.trim().toLowerCase()
    return array.find((s) => s.word.toLowerCase().startsWith(prefix))
}