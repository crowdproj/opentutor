package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.cards(service: CardService) {
    route("cards") {
        post("create") {
            call.createCard(service)
        }
        post("update") {
            call.updateCard(service)
        }
        post("search") {
            call.searchCards(service)
        }
        post("get") {
            call.getCard(service)
        }
        post("learn") {
            call.learnCard(service)
        }
        post("reset") {
            call.resetCard(service)
        }
        post("delete") {
            call.deleteCard(service)
        }
    }
}
