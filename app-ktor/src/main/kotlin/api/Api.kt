package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.api.controllers.dictionaries
import com.gitlab.sszuev.flashcards.api.controllers.sounds
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

internal fun Route.cardApiV1(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        cards(service, repositories, contextConfig)
        sounds(service, repositories, contextConfig)
    }
}

internal fun Route.dictionaryApiV1(
    service: DictionaryService,
    repositories: AppRepositories,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        dictionaries(service, repositories, contextConfig)
    }
}