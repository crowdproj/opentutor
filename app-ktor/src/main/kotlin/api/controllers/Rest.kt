package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.cards(
    service: CardService,
    contextConfig: ContextConfig,
) {
    route("cards") {
        post("create") {
            call.createCard(service, contextConfig)
        }
        post("update") {
            call.updateCard(service, contextConfig)
        }
        post("search") {
            call.searchCards(service, contextConfig)
        }
        post("get-all") {
            call.getAllCards(service, contextConfig)
        }
        post("get") {
            call.getCard(service, contextConfig)
        }
        post("learn") {
            call.learnCard(service, contextConfig)
        }
        post("reset") {
            call.resetCard(service, contextConfig)
        }
        post("delete") {
            call.deleteCard(service, contextConfig)
        }
    }
}

fun Route.sounds(
    service: CardService,
    contextConfig: ContextConfig,
) {
    route("sounds") {
        post("get") {
            call.getResource(service, contextConfig)
        }
    }
}

fun Route.dictionaries(
    service: DictionaryService,
    contextConfig: ContextConfig,
) {
    route("dictionaries") {
        post("get-all") {
            call.getAllDictionaries(service, contextConfig)
        }
        post("create") {
            call.createDictionary(service, contextConfig)
        }
        post("delete") {
            call.deleteDictionary(service, contextConfig)
        }
        post("download") {
            call.downloadDictionary(service, contextConfig)
        }
        post("upload") {
            call.uploadDictionary(service, contextConfig)
        }
    }
}