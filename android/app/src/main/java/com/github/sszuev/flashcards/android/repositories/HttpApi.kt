package com.github.sszuev.flashcards.android.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.github.sszuev.flashcards.android.AppConfig
import com.github.sszuev.flashcards.android.AppContextProvider
import com.github.sszuev.flashcards.android.defaultHttpClient
import com.github.sszuev.flashcards.android.lightHttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import kotlinx.serialization.Serializable
import java.net.UnknownHostException

suspend inline fun <reified T> authPost(
    url: String,
    withRetry: Boolean = true,
    crossinline configureRequest: HttpRequestBuilder.() -> Unit,
): T {
    Log.d("HttpApi", "POST :: <$url> -- START")
    var token = getAccessToken()
    return try {
        val client = if (withRetry) defaultHttpClient else lightHttpClient
        client.post(url) {
            headers.append("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            configureRequest()
        }.body<T>()
    } catch (e: ResponseException) {
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> {
                Log.d("HttpApi", "Received 401. Attempting to refresh token.")
                refreshToken()
                token = getAccessToken()
                val res: T = defaultHttpClient.post(url) {
                    headers.append("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    configureRequest()
                }.body()
                Log.d("HttpApi", "second attempt succeeds")
                res
            }

            HttpStatusCode.BadRequest -> {
                Log.w("HttpApi", "400 Bad Request: ${e.response}")
                throw InvalidTokenException("Session expired. Please log in again.", e)
            }

            else -> {
                throw e
            }
        }
    } catch (e: Exception) {
        throw e.toClientException()
    } finally {
        Log.d("HttpApi", "POST :: <$url> -- END")
    }
}

fun getAccessToken(): String {
    val prefs = AppContextProvider.getContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
    return prefs.getString("access_token", "")
        ?: throw IllegalStateException("Access token is missing")
}

suspend fun refreshToken() {
    Log.d("HttpApi", "Refreshing access token")
    val prefs = AppContextProvider.getContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
    val refreshToken = prefs.getString("refresh_token", null)
        ?: throw IllegalStateException("Refresh token is missing")

    val response: TokenResponse = try {
        defaultHttpClient.post("${AppConfig.serverUri}/realms/flashcards-realm/protocol/openid-connect/token") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "grant_type" to "refresh_token",
                    "client_id" to "flashcards-android",
                    "refresh_token" to refreshToken
                ).formUrlEncode()
            )
        }.body()
    } catch (e: ResponseException) {
        if (e.response.status == HttpStatusCode.BadRequest) {
            Log.w("HttpApi", "400 Bad Request: ${e.response}")
            throw InvalidTokenException("Session expired. Please log in again.", e)
        } else {
            throw e
        }
    } catch (e: Exception) {
        throw e.toClientException()
    }
    prefs.edit {
        putString("access_token", response.access_token)
            .putString("refresh_token", response.refresh_token)
    }
    Log.d("HttpApi", "refresh succeeds")
}

@Suppress("PropertyName")
@Serializable
data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
)

fun Exception.toClientException(): Exception {
    return when (this) {
        is UnknownHostException -> {
            Log.e("HttpApi", "Network error: server unavailable", this)
            ServerUnavailableException(
                "Server is unreachable. Check your connection, then press HOME to refresh the page.",
                this
            )
        }

        is SocketTimeoutException,
        is ConnectTimeoutException,
        is HttpRequestTimeoutException -> {
            Log.e("HttpApi", "Timeout", this)
            ServerUnavailableException(
                "Server is taking too long to respond. Press HOME to refresh the page.",
                this
            )
        }

        else -> UnknownConnectionException(
            "Something went wrong. Press HOME to refresh the page.",
            this
        )
    }
}

class ServerUnavailableException(message: String, cause: Throwable) : Exception(message, cause)

class InvalidTokenException(message: String, cause: Exception) : Exception(message, cause)

class UnknownConnectionException(message: String, cause: Exception) : Exception(message, cause)