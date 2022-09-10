package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.controllers.cards
import com.gitlab.sszuev.flashcards.api.controllers.dictionaries
import com.gitlab.sszuev.flashcards.api.controllers.sounds
import com.gitlab.sszuev.flashcards.logslib.LogbackWrapper
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import io.ktor.server.routing.*

internal fun Route.apiV1(cardService: CardService, dictionaryService: DictionaryService, logger: LogbackWrapper) {
    route("v1/api") {
        cards(cardService, logger)
        sounds(cardService, logger)
        dictionaries(dictionaryService, logger)
    }
}