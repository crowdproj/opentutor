package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.services.DictionaryService
import com.gitlab.sszuev.flashcards.config.RunConfig
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.cards(
    service: CardService,
    repositories: CardRepositories,
    runConf: RunConfig = RunConfig.PROD
) {
    route("cards") {
        post("create") {
            call.createCard(service, repositories, runConf)
        }
        post("update") {
            call.updateCard(service, repositories, runConf)
        }
        post("search") {
            call.searchCards(service, repositories, runConf)
        }
        post("get-all") {
            call.getAllCards(service, repositories, runConf)
        }
        post("get") {
            call.getCard(service, repositories, runConf)
        }
        post("learn") {
            call.learnCard(service, repositories, runConf)
        }
        post("reset") {
            call.resetCard(service, repositories, runConf)
        }
        post("delete") {
            call.deleteCard(service, repositories, runConf)
        }
    }
}

fun Route.sounds(
    service: CardService,
    repositories: CardRepositories,
    runConf: RunConfig = RunConfig.PROD
) {
    route("sounds") {
        post("get") {
            call.getResource(service, repositories, runConf)
        }
    }
}

fun Route.dictionaries(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConf: RunConfig = RunConfig.PROD
) {
    route("dictionaries") {
        post("get-all") {
            call.getAllDictionaries(service, repositories, runConf)
        }
        post("create") {
            call.createDictionary(service, repositories, runConf)
        }
        post("delete") {
            call.deleteDictionary(service, repositories, runConf)
        }
        post("download") {
            call.downloadDictionary(service, repositories, runConf)
        }
        post("upload") {
            call.uploadDictionary(service, repositories, runConf)
        }
    }
}