package com.github.sszuev.flashcards.android.repositories

import android.content.Context
import android.util.Log
import com.github.sszuev.flashcards.android.AppConfig
import com.github.sszuev.flashcards.android.AppContextProvider
import com.github.sszuev.flashcards.android.httpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
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
    crossinline configureRequest: HttpRequestBuilder.() -> Unit,
): T {
    Log.d("HttpApi", "POST :: <$url>")
    var token = getAccessToken()
    return try {
        httpClient.post(url) {
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
                val res: T = httpClient.post(url) {
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
    } catch (e: UnknownHostException) {
        Log.e("HttpApi", "Network error: server unavailable", e)
        throw ServerUnavailableException("Server is unreachable. Check your connection.", e)
    } catch (e: SocketTimeoutException) {
        Log.e("HttpApi", "Network timeout", e)
        throw ServerUnavailableException("Server is taking too long to respond.", e)
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
        httpClient.post("${AppConfig.serverUri}/realms/flashcards-realm/protocol/openid-connect/token") {
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
    } catch (e: UnknownHostException) {
        Log.e("HttpApi", "Network error: server unavailable", e)
        throw ServerUnavailableException("Server is unreachable. Check your connection.", e)
    } catch (e: SocketTimeoutException) {
        Log.e("HttpApi", "Network timeout", e)
        throw ServerUnavailableException("Server is taking too long to respond.", e)
    }
    prefs.edit()
        .putString("access_token", response.access_token)
        .putString("refresh_token", response.refresh_token)
        .apply()
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

class ServerUnavailableException(message: String, cause: Throwable) : Exception(message, cause)

class InvalidTokenException(message: String, cause: Exception) : Exception(message, cause)