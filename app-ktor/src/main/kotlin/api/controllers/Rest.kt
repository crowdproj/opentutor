package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.logslib.LogbackWrapper
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.cards(service: CardService, logger: LogbackWrapper) {
    route("cards") {
        post("create") {
            call.createCard(service, logger)
        }
        post("update") {
            call.updateCard(service, logger)
        }
        post("search") {
            call.searchCards(service, logger)
        }
        post("get-all") {
            call.getAllCards(service, logger)
        }
        post("get") {
            call.getCard(service, logger)
        }
        post("learn") {
            call.learnCard(service, logger)
        }
        post("reset") {
            call.resetCard(service, logger)
        }
        post("delete") {
            call.deleteCard(service, logger)
        }
    }
}

fun Route.sounds(service: CardService, logger: LogbackWrapper) {
    route("sounds") {
        post("get") {
            call.getResource(service, logger)
        }
    }
}

fun Route.dictionaries(service: DictionaryService, logger: LogbackWrapper) {
    route("dictionaries") {
        post("get-all") {
            call.getAllDictionaries(service, logger)
        }
    }
}