package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppOperation

enum class SettingsOperation : AppOperation {
    NONE,
    GET_SETTINGS,
    UPDATE_SETTINGS,
}