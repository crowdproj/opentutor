package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.SettingsService
import com.gitlab.sszuev.flashcards.services.TTSService
import com.gitlab.sszuev.flashcards.services.TranslationService
import com.gitlab.sszuev.flashcards.services.local.LocalCardService
import com.gitlab.sszuev.flashcards.services.local.LocalDictionaryService
import com.gitlab.sszuev.flashcards.services.local.LocalSettingsService
import com.gitlab.sszuev.flashcards.services.local.LocalTTSService
import com.gitlab.sszuev.flashcards.services.local.LocalTranslationService
import com.gitlab.sszuev.flashcards.services.remote.RemoteCardService
import com.gitlab.sszuev.flashcards.services.remote.RemoteDictionaryService
import com.gitlab.sszuev.flashcards.services.remote.RemoteSettingsService
import com.gitlab.sszuev.flashcards.services.remote.RemoteTTSService

internal fun cardService(config: RunConfig): CardService = if (config.mode == RunConfig.Mode.TEST)
    LocalCardService()
else
    RemoteCardService()

internal fun dictionaryService(config: RunConfig): DictionaryService = if (config.mode == RunConfig.Mode.TEST)
    LocalDictionaryService()
else
    RemoteDictionaryService()

internal fun ttsService(config: RunConfig): TTSService = if (config.mode == RunConfig.Mode.TEST)
    LocalTTSService()
else
    RemoteTTSService()

internal fun translationService(config: RunConfig): TranslationService = if (config.mode == RunConfig.Mode.TEST)
    LocalTranslationService()
else
    TODO()

internal fun settingsService(config: RunConfig): SettingsService = if (config.mode == RunConfig.Mode.TEST)
    LocalSettingsService()
else
    RemoteSettingsService()