package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.api.controllers.dictionaries
import com.gitlab.sszuev.flashcards.api.controllers.sounds
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.services.DictionaryService
import com.gitlab.sszuev.flashcards.config.ContextConfig
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

internal fun Route.cardApiV1(
    service: CardService,
    repositories: CardRepositories,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        cards(service, repositories, contextConfig)
        sounds(service, repositories, contextConfig)
    }
}

internal fun Route.dictionaryApiV1(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        dictionaries(service, repositories, contextConfig)
    }
}