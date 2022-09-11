package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.cards(service: CardService, runConf: RunConfig = RunConfig.PROD) {
    route("cards") {
        post("create") {
            call.createCard(service, runConf)
        }
        post("update") {
            call.updateCard(service, runConf)
        }
        post("search") {
            call.searchCards(service, runConf)
        }
        post("get-all") {
            call.getAllCards(service, runConf)
        }
        post("get") {
            call.getCard(service, runConf)
        }
        post("learn") {
            call.learnCard(service, runConf)
        }
        post("reset") {
            call.resetCard(service, runConf)
        }
        post("delete") {
            call.deleteCard(service, runConf)
        }
    }
}

fun Route.sounds(service: CardService, runConf: RunConfig = RunConfig.PROD) {
    route("sounds") {
        post("get") {
            call.getResource(service, runConf)
        }
    }
}

fun Route.dictionaries(service: DictionaryService, runConf: RunConfig = RunConfig.PROD) {
    route("dictionaries") {
        post("get-all") {
            call.getAllDictionaries(service, runConf)
        }
    }
}