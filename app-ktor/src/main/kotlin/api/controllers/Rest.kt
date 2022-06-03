package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.cards(service: CardService) {
    route("cards") {
        post("create") {
            call.createCard(service)
        }
    }
}


