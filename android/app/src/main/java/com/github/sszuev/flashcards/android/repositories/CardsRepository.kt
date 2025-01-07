package com.github.sszuev.flashcards.android.repositories

import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import java.util.UUID

class CardsRepository(
    private val serverUri: String,
) {
    private val tag = "CardsRepository"

    suspend fun getAll(dictionaryId: String): List<CardResource> {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Get all cards with requestId=$requestId and dictionaryId=$dictionaryId")
        val container = authPost<GetAllCardsResponse>("$serverUri/v1/api/cards/get-all") {
            setBody(
                GetAllCardsRequest(
                    requestType = "getAllCards",
                    requestId = requestId,
                    dictionaryId = dictionaryId,
                )
            )
        }
        Log.d(
            tag,
            "Received response for requestId: $requestId, cards count: ${container.cards.size}"
        )
        handleErrors(container)
        return container.cards
    }

    suspend fun updateCard(card: CardResource) {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Update card with requestId=$requestId and cardId=${card.cardId}")
        val container = authPost<UpdateCardResponse>("$serverUri/v1/api/cards/update") {
            setBody(
                UpdateCardRequest(
                    requestType = "updateCard",
                    requestId = requestId,
                    card = card,
                )
            )
        }
        Log.d(
            tag,
            "Received response for requestId: $requestId"
        )
        handleErrors(container)
    }

    suspend fun createCard(card: CardResource): CardResource {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Create card with requestId=$requestId")
        val container = authPost<CreateCardResponse>("$serverUri/v1/api/cards/create") {
            setBody(
                CreateCardRequest(
                    requestType = "createCard",
                    requestId = requestId,
                    card = card,
                )
            )
        }
        Log.d(
            tag,
            "Received response for requestId: $requestId, response: $container"
        )
        handleErrors(container)
        return checkNotNull(container.card) { "null card received" }
    }

    suspend fun deleteCard(cardId: String) {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Delete card with requestId=$requestId and cardId=$cardId")
        val container = authPost<DeleteCardResponse>("$serverUri/v1/api/cards/delete") {
            setBody(
                DeleteCardRequest(
                    requestType = "deleteCard",
                    requestId = requestId,
                    cardId = cardId,
                )
            )
        }
        Log.d(tag, "Received response for requestId: $requestId, response: $container")
        handleErrors(container)
    }

    suspend fun resetCard(cardId: String) {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Reset card with requestId=$requestId and cardId=$cardId")
        val container = authPost<ResetCardResponse>("$serverUri/v1/api/cards/reset") {
            setBody(
                ResetCardRequest(
                    requestType = "resetCard",
                    requestId = requestId,
                    cardId = cardId,
                )
            )
        }
        Log.d(tag, "Received response for requestId: $requestId, response: $container")
        handleErrors(container)
    }
}

@Suppress("unused")
@Serializable
private data class GetAllCardsRequest(
    override val requestType: String,
    override val requestId: String,
    val dictionaryId: String? = null,
) : BaseRequest

@Serializable
private data class GetAllCardsResponse(
    override val requestId: String,
    val cards: List<CardResource> = emptyList(),
    override val errors: List<ErrorResource>? = null,
) : BaseResponse

@Serializable
private data class UpdateCardRequest(
    override val requestType: String,
    override val requestId: String,
    val card: CardResource,
) : BaseRequest

@Serializable
private data class UpdateCardResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
) : BaseResponse

@Serializable
private data class CreateCardRequest(
    override val requestType: String,
    override val requestId: String,
    val card: CardResource,
) : BaseRequest

@Serializable
private data class CreateCardResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
    val card: CardResource? = null,
) : BaseResponse

@Serializable
private data class DeleteCardRequest(
    override val requestType: String,
    override val requestId: String,
    val cardId: String,
) : BaseRequest

@Serializable
private data class DeleteCardResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null
) : BaseResponse

@Serializable
private data class ResetCardRequest(
    override val requestType: String,
    override val requestId: String,
    val cardId: String,
) : BaseRequest

@Serializable
private data class ResetCardResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
    val card: CardResource? = null,
) : BaseResponse