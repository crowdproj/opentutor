package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.HealthService
import com.gitlab.sszuev.flashcards.services.SettingsService
import com.gitlab.sszuev.flashcards.services.TTSService
import com.gitlab.sszuev.flashcards.services.TranslationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
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
        post("reset-all") {
            call.resetAllCards(service, contextConfig)
        }
    }
}

fun Route.sounds(
    service: TTSService,
    contextConfig: ContextConfig,
) {
    route("sounds") {
        post("get") {
            call.getResource(service, contextConfig)
        }
    }
}

fun Route.translation(
    service: TranslationService,
    contextConfig: ContextConfig,
) {
    route("translation") {
        post("fetch") {
            call.fetchTranslation(service, contextConfig)
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
        post("update") {
            call.updateDictionary(service, contextConfig)
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

fun Route.settings(
    service: SettingsService,
    contextConfig: ContextConfig,
) {
    route("settings") {
        post("get") {
            call.getSettings(service, contextConfig)
        }
        post("update") {
            call.updateSettings(service, contextConfig)
        }
    }
}

fun Route.health(service: HealthService) {
    get("health") {
        if (service.ping()) {
            call.respondText("OK", status = HttpStatusCode.OK)
        } else {
            call.respondText("Unhealthy", status = HttpStatusCode.ServiceUnavailable)
        }
    }
}