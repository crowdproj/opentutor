package com.github.sszuev.flashcards.android.repositories

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorResource(
    val code: String? = null,
    val group: String? = null,
    val `field`: String? = null,
    val message: String? = null,
)

internal interface BaseRequest {
    val requestType: String
    val requestId: String
}

internal interface BaseResponse {
    val requestId: String
    val errors: List<ErrorResource>?
}

internal fun handleErrors(container: BaseResponse) {
    if (container.errors?.isNotEmpty() == true) {
        val error = RuntimeException(
            "ERRORS:\n${
                checkNotNull(container.errors)
                    .map { it.message }
                    .joinToString(separator = "\n")
            }"
        )
        Log.e("Repositories", "Server error.", error)
        throw ApiResponseException("Server error. Press HOME to refresh the page.", error)
    }
}

class ApiResponseException(message: String, cause: Throwable?) :
    IllegalStateException(message, cause)