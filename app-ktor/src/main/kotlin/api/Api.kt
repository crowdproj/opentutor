package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.routing.*

internal fun Routing.apiV1(service: CardService) {
    route("v1/api") {
        cards(service)
    }
}
