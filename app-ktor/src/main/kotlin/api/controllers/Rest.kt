package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.services.DictionaryService
import com.gitlab.sszuev.flashcards.config.ContextConfig
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.cards(
    service: CardService,
    repositories: CardRepositories,
    contextConfig: ContextConfig,
) {
    route("cards") {
        post("create") {
            call.createCard(service, repositories, contextConfig)
        }
        post("update") {
            call.updateCard(service, repositories, contextConfig)
        }
        post("search") {
            call.searchCards(service, repositories, contextConfig)
        }
        post("get-all") {
            call.getAllCards(service, repositories, contextConfig)
        }
        post("get") {
            call.getCard(service, repositories, contextConfig)
        }
        post("learn") {
            call.learnCard(service, repositories, contextConfig)
        }
        post("reset") {
            call.resetCard(service, repositories, contextConfig)
        }
        post("delete") {
            call.deleteCard(service, repositories, contextConfig)
        }
    }
}

fun Route.sounds(
    service: CardService,
    repositories: CardRepositories,
    contextConfig: ContextConfig,
) {
    route("sounds") {
        post("get") {
            call.getResource(service, repositories, contextConfig)
        }
    }
}

fun Route.dictionaries(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig,
) {
    route("dictionaries") {
        post("get-all") {
            call.getAllDictionaries(service, repositories, contextConfig)
        }
        post("create") {
            call.createDictionary(service, repositories, contextConfig)
        }
        post("delete") {
            call.deleteDictionary(service, repositories, contextConfig)
        }
        post("download") {
            call.downloadDictionary(service, repositories, contextConfig)
        }
        post("upload") {
            call.uploadDictionary(service, repositories, contextConfig)
        }
    }
}