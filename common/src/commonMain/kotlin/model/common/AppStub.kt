package com.gitlab.sszuev.flashcards.model.common

/**
 * Describes stub cases.
 */
enum class AppStub {
    NONE,
    SUCCESS,
    UNKNOWN_ERROR,

    ERROR_UNEXPECTED_FIELD,

    ERROR_WRONG_CARD_ID,
    ERROR_WRONG_DICTIONARY_ID,
    ERROR_CARD_WRONG_WORD,
    ERROR_CARD_WRONG_TRANSCRIPTION,
    ERROR_CARD_WRONG_TRANSLATION,
    ERROR_CARD_WRONG_EXAMPLES,
    ERROR_CARD_WRONG_PART_OF_SPEECH,
    ERROR_CARD_WRONG_DETAILS,
    ERROR_CARD_WRONG_AUDIO_RESOURCE,

    ERROR_AUDIO_RESOURCE_WRONG_RESOURCE_ID,
    ERROR_AUDIO_RESOURCE_NOT_FOUND,
    ERROR_AUDIO_RESOURCE_SERVER_ERROR,

    ERROR_CARDS_WRONG_FILTER_LENGTH,

    ERROR_LEARN_CARD_WRONG_CARD_ID,
    ERROR_LEARN_CARD_WRONG_STAGES,
    ERROR_LEARN_CARD_WRONG_DETAILS,
}