package com.github.sszuev.flashcards.android.repositories

import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import java.util.UUID

class SettingsRepository(
    private val serverUri: String,
) {
    private val tag = "SettingsRepository"

    suspend fun get(): SettingsResource {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Get settings with requestId=$requestId")
        val container =
            authPost<GetSettingsResponse>("$serverUri/v1/api/settings/get") {
                setBody(
                    GetSettingsRequest(
                        requestType = "getSettings",
                        requestId = requestId,
                    )
                )
            }
        handleErrors(container)
        Log.d(
            tag,
            "Received response for requestId: $requestId"
        )
        return container.settings
    }

    suspend fun update(settings: SettingsResource) {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Update settings with requestId=$requestId")
        val container =
            authPost<UpdateSettingsResponse>("$serverUri/v1/api/settings/update") {
                setBody(
                    UpdateSettingsRequest(
                        requestType = "updateSettings",
                        requestId = requestId,
                        settings = settings,
                    )
                )
            }
        handleErrors(container)
        Log.d(
            tag,
            "Received response for requestId: $requestId"
        )
    }
}

@Serializable
private class GetSettingsRequest(
    override val requestType: String,
    override val requestId: String,
) : BaseRequest

@Serializable
private data class GetSettingsResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
    val settings: SettingsResource,
) : BaseResponse

@Serializable
private data class UpdateSettingsRequest(
    override val requestType: String,
    override val requestId: String,
    val settings: SettingsResource,
) : BaseRequest

@Serializable
private data class UpdateSettingsResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
) : BaseResponse