package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.api.controllers.dictionaries
import com.gitlab.sszuev.flashcards.api.controllers.sounds
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.TTSService
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

internal fun Route.cardApiV1(
    service: CardService,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        cards(service, contextConfig)
    }
}

internal fun Route.dictionaryApiV1(
    service: DictionaryService,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        dictionaries(service, contextConfig)
    }
}

internal fun Route.ttsApiV1(
    service: TTSService,
    contextConfig: ContextConfig,
) {
    route("v1/api") {
        sounds(service, contextConfig)
    }
}