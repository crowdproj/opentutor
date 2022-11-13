package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppOperation

enum class DictionaryOperation: AppOperation {
    NONE,
    GET_ALL_DICTIONARIES,
    CREATE_DICTIONARY,
    DELETE_DICTIONARY,
    DOWNLOAD_DICTIONARY,
    UPLOAD_DICTIONARY,
}