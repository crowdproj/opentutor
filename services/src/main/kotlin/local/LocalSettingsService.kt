package com.gitlab.sszuev.flashcards.services.local

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.core.SettingsCorProcessor
import com.gitlab.sszuev.flashcards.services.SettingsService
import com.gitlab.sszuev.flashcards.services.localDbRepositories

class LocalSettingsService : SettingsService {
    private val processor = SettingsCorProcessor()

    override suspend fun getSettings(context: SettingsContext): SettingsContext = context.exec()
    override suspend fun updateSettings(context: SettingsContext): SettingsContext = context.exec()

    private suspend fun SettingsContext.exec(): SettingsContext {
        this.repositories = localDbRepositories
        processor.execute(this)
        return this
    }
}