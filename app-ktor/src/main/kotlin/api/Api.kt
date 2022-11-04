package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.api.controllers.dictionaries
import com.gitlab.sszuev.flashcards.api.controllers.sounds
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.services.DictionaryService
import com.gitlab.sszuev.flashcards.config.RunConfig
import io.ktor.server.routing.*

internal fun Route.cardApiV1(
    service: CardService,
    repositories: CardRepositories,
    runConfig: RunConfig = RunConfig.PROD,
) {
    route("v1/api") {
        cards(service, repositories, runConfig)
        sounds(service, repositories, runConfig)
    }
}

internal fun Route.dictionaryApiV1(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConfig: RunConfig = RunConfig.PROD,
) {
    route("v1/api") {
        dictionaries(service, repositories, runConfig)
    }
}