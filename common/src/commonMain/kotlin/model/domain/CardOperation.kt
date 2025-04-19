package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppOperation

enum class CardOperation: AppOperation {
    NONE,
    SEARCH_CARDS,
    GET_ALL_CARDS,
    GET_CARD,
    CREATE_CARD,
    UPDATE_CARD,
    DELETE_CARD,
    LEARN_CARDS,
    RESET_CARDS,
    RESET_CARD,
}

