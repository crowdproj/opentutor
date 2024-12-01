package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppOperation

enum class TranslationOperation : AppOperation {
    NONE,
    FETCH_CARD,
}