package com.github.sszuev.flashcards.android.repositories

import android.content.Context
import android.util.Log
import com.github.sszuev.flashcards.android.AppConfig
import com.github.sszuev.flashcards.android.AppContextProvider
import com.github.sszuev.flashcards.android.clientProducer
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

suspend inline fun <reified T> authPost(
    url: String,
    crossinline configureRequest: HttpRequestBuilder.() -> Unit,
): T = withContext(Dispatchers.IO) {
    Log.d("HttpApi", "POST :: <$url>")
    var token = getAccessToken()
    val client = clientProducer()
    try {
        val res: T =
            client.use {
                it.post(url) {
                    headers.append("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    configureRequest()
                }.body()
            }
        res
    } catch (e: ResponseException) {
        if (e.response.status == HttpStatusCode.Unauthorized) {
            Log.d("HttpApi", "Received 401. Attempting to refresh token.")
            if (refreshToken()) {
                token = getAccessToken()
                client.use {
                    it.post(url) {
                        headers.append("Authorization", "Bearer $token")
                        contentType(ContentType.Application.Json)
                        configureRequest()
                    }.body()
                }
            } else {
                throw IllegalStateException("Token refresh failed")
            }
        } else {
            throw e
        }
    }
}

fun getAccessToken(): String {
    val prefs = AppContextProvider.getContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
    return prefs.getString("access_token", "")
        ?: throw IllegalStateException("Access token is missing")
}

suspend fun refreshToken(): Boolean {
    Log.d("HttpApi", "Refresh token")
    val prefs = AppContextProvider.getContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
    val refreshToken = prefs.getString("refresh_token", null)
        ?: throw IllegalStateException("Refresh token is missing")

    val client = clientProducer()
    return try {
        val response: TokenResponse = client.use {
            it.post("${AppConfig.serverUri}/realms/flashcards-realm/protocol/openid-connect/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    listOf(
                        "grant_type" to "refresh_token",
                        "client_id" to "flashcards-android",
                        "refresh_token" to refreshToken
                    ).formUrlEncode()
                )
            }.body()
        }

        prefs.edit()
            .putString("access_token", response.access_token)
            .putString("refresh_token", response.refresh_token)
            .apply()
        true
    } catch (e: Exception) {
        Log.e("HttpApi", "Token refresh failed", e)
        false
    }
}

@Suppress("PropertyName")
@Serializable
data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
)