package com.github.sszuev.flashcards.android.repositories

import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import java.util.UUID

class TranslationRepository(
    private val serverUri: String,
) {

    private val tag = "TranslationRepository"

    suspend fun fetch(query: String, sourceLang: String, targetLang: String): CardResource {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Fetch translation with requestId=$requestId")
        val container =
            authPost<FetchTranslationResponse>("$serverUri/v1/api/translation/fetch") {
                setBody(
                    FetchTranslationRequest(
                        requestType = "fetchTranslation",
                        requestId = requestId,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                        word = query,
                    )
                )
            }
        if (container.errors?.isNotEmpty() == true) {
            Log.e(
                tag,
                "ERRORS::${
                    checkNotNull(container.errors).joinToString(
                        separator = "' ",
                        prefix = "'",
                        postfix = "'"
                    ) { it.message ?: "unknown error" }
                }"
            )
        }
        Log.d(tag, "Received response for requestId: $requestId")
        val res = container.card
        Log.d(tag, "Fetch result: $res")
        return res ?: CardResource()
    }
}

@Serializable
data class FetchTranslationRequest(
    override val requestType: String,
    override val requestId: String,
    val sourceLang: String,
    val targetLang: String,
    val word: String,
) : BaseRequest

@Serializable
private data class FetchTranslationResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
    val card: CardResource? = null,
) : BaseResponse