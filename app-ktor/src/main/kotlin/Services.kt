package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.local.LocalCardService
import com.gitlab.sszuev.flashcards.services.local.LocalDictionaryService
import com.gitlab.sszuev.flashcards.services.remote.RemoteCardService
import com.gitlab.sszuev.flashcards.services.remote.RemoteDictionaryService

internal fun cardService(config: RunConfig): CardService = if (config.mode == RunConfig.Mode.TEST)
    LocalCardService()
else
    RemoteCardService()

internal fun dictionaryService(config: RunConfig): DictionaryService = if (config.mode == RunConfig.Mode.TEST)
    LocalDictionaryService()
else
    RemoteDictionaryService()
