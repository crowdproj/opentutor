package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.api.controllers.sounds
import com.gitlab.sszuev.flashcards.logslib.LogbackWrapper
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.routing.*

internal fun Route.apiV1(service: CardService, logger: LogbackWrapper) {
    route("v1/api") {
        cards(service, logger)
        sounds(service, logger)
    }
}