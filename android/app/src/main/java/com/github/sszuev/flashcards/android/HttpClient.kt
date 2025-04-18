package com.github.sszuev.flashcards.android

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val httpClient: HttpClient = HttpClient(Android) {
    install(HttpTimeout) {
        requestTimeoutMillis = 9_000
        connectTimeoutMillis = 5_000
        socketTimeoutMillis = 10_000
    }
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}