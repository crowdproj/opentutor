package com.github.sszuev.flashcards.android

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

val httpClient: HttpClient = HttpClient(Android) {
    install(HttpTimeout) {
        requestTimeoutMillis = 3000
        connectTimeoutMillis = 1500
        socketTimeoutMillis = 2500
    }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 4)
        retryOnExceptionIf(maxRetries = 4) { _, cause ->
            when (cause) {
                is ResponseException -> {
                    val code = cause.response.status.value
                    code !in listOf(400, 401, 403)
                }

                is SerializationException -> false
                is CancellationException -> false
                else -> true
            }
        }
        exponentialDelay()
    }
    if (BuildConfig.DEBUG) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("HTTP", message)
                }
            }
            level = LogLevel.ALL
        }
    }
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}