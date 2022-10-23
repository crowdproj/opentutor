package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppOperation

enum class DictionaryOperation: AppOperation {
    NONE,
    GET_ALL_DICTIONARIES,
    DELETE_DICTIONARY,
}