package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.mappers.v1.fromTransport
import com.gitlab.sszuev.flashcards.mappers.v1.toCreateCardResponse
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

suspend fun ApplicationCall.createCard(service: CardService) {
    val createCardRequest = receive<CreateCardRequest>()
    respond(
        AppContext().apply {
            fromTransport(createCardRequest)
        }.let {
            service.createCardEntity(it)
        }.toCreateCardResponse()
    )
}
