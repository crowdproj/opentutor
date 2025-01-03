package com.github.sszuev.flashcards.android.repositories

import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import java.util.UUID

class DictionaryRepository(
    private val serverUri: String,
) {
    private val tag = "DictionaryRepository"

    suspend fun getAll(): List<DictionaryResource> {
        val requestId = UUID.randomUUID().toString()
        Log.i(tag, "Get all dictionaries with requestId: $requestId")
        val container = authPost<GetAllDictionariesResponse>("$serverUri/v1/api/dictionaries/get-all") {
            setBody(
                GetAllDictionariesRequest(
                    requestType = "getAllDictionaries",
                    requestId = requestId,
                )
            )
        }
        Log.i(
            tag,
            "Received response for requestId: $requestId, dictionaries count: ${container.dictionaries.size}"
        )
        return container.dictionaries
    }

    @Serializable
    private data class GetAllDictionariesResponse(
        val dictionaries: List<DictionaryResource>
    )

    @Suppress("unused")
    @Serializable
    private class GetAllDictionariesRequest(
        val requestType: String,
        val requestId: String
    )
}

