package com.gitlab.sszuev.flashcards.model.domain

enum class CardOperation {
    NONE,
    SEARCH_CARDS,
    GET_CARD,
    CREATE_CARD,
    UPDATE_CARD,
    DELETE_CARD,
    LEARN_CARD,
    RESET_CARD,
}