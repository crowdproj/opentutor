package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.mappers.v1.*
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

suspend fun ApplicationCall.createCard(service: CardService) {
    val createCardRequest = receive<CreateCardRequest>()
    respond(
        CardContext().apply {
            fromCreateCardRequest(createCardRequest)
        }.let {
            service.createCard(it)
        }.toCreateCardResponse()
    )
}

suspend fun ApplicationCall.updateCard(service: CardService) {
    val updateCardRequest = receive<UpdateCardRequest>()
    respond(
        CardContext().apply {
            fromUpdateCardRequest(updateCardRequest)
        }.let {
            service.createCard(it)
        }.toUpdateCardResponse()
    )
}

suspend fun ApplicationCall.searchCards(service: CardService) {
    val getCardsRequest = receive<GetCardsRequest>()
    respond(
        CardContext().apply {
            fromGetCardsRequest(getCardsRequest)
        }.let {
            service.searchCards(it)
        }.toGetCardsResponse()
    )
}

suspend fun ApplicationCall.getCard(service: CardService) {
    val getCardRequest = receive<GetCardRequest>()
    respond(
        CardContext().apply {
            fromGetCardRequest(getCardRequest)
        }.let {
            service.getCard(it)
        }.toGetCardResponse()
    )
}

suspend fun ApplicationCall.learnCard(service: CardService) {
    val learnCardRequest = receive<LearnCardRequest>()
    respond(
        CardContext().apply {
            fromLearnCardRequest(learnCardRequest)
        }.let {
            service.learnCard(it)
        }.toLearnCardResponse()
    )
}

suspend fun ApplicationCall.resetCard(service: CardService) {
    val resetCardRequest = receive<ResetCardRequest>()
    respond(
        CardContext().apply {
            fromResetCardRequest(resetCardRequest)
        }.let {
            service.resetCard(it)
        }.toResetCardResponse()
    )
}

suspend fun ApplicationCall.deleteCard(service: CardService) {
    val deleteCardRequest = receive<DeleteCardRequest>()
    respond(
        CardContext().apply {
            fromDeleteCardRequest(deleteCardRequest)
        }.let {
            service.deleteCard(it)
        }.toDeleteCardResponse()
    )
}