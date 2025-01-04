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
        return container.cards
    }

    @Serializable
    private data class GetAllCardsResponse(
        val responseType: String? = null,
        val requestId: String? = null,
        val cards: List<CardResource> = emptyList()
    )

    @Suppress("unused")
    @Serializable
    data class GetAllCardsRequest(
        val requestType: String? = null,
        val requestId: String? = null,
        val dictionaryId: String? = null
    )
}