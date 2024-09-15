package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.SettingsContext

interface SettingsService {

    suspend fun getSettings(context: SettingsContext): SettingsContext

    suspend fun updateSettings(context: SettingsContext): SettingsContext
}