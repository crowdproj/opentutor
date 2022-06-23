package com.gitlab.sszuev.flashcards.model.common

/**
 * Describes stub cases.
 */
enum class AppStub {
    NONE,
    SUCCESS,
    ERROR_CARD_WRONG_WORD,
    ERROR_CARD_WRONG_TRANSCRIPTION,
    ERROR_CARD_WRONG_TRANSLATION,
    ERROR_CARD_WRONG_EXAMPLES,
    ERROR_CARD_WRONG_PART_OF_SPEECH,
    ERROR_CARD_WRONG_DETAILS,
    ERROR_CARD_WRONG_AUDIO_RESOURCE,
    UNKNOWN_ERROR,
}