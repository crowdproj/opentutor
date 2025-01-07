package com.github.sszuev.flashcards.android.repositories

import android.util.Base64
import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import java.util.UUID

class TTSRepository(
    private val serverUri: String,
) {
    private val tag = "TTSRepository"

    suspend fun get(lang: String, word: String): ByteArray? {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Get all dictionaries with requestId=$requestId")
        val container =
            authPost<GetAudioResponse>("$serverUri/v1/api/sounds/get") {
                setBody(
                    GetAudioRequest(
                        requestType = "getAudio",
                        requestId = requestId,
                        lang = lang,
                        word = word
                    )
                )
            }
        handleErrors(container)
        Log.d(
            tag,
            "Received response for requestId: $requestId"
        )
        return container.resource?.let {
            Base64.decode(it, Base64.DEFAULT)
        }
    }
}

@Serializable
private data class GetAudioRequest(
    override val requestType: String,
    override val requestId: String,
    val lang: String,
    val word: String,
): BaseRequest

@Serializable
private data class GetAudioResponse (
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
    val resource: String? = null,
) : BaseResponse