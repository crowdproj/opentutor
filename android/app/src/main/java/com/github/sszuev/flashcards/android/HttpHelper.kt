package com.github.sszuev.flashcards.android

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val clientProducer: () -> HttpClient = {
    HttpClient(Android) {
        install(HttpTimeout)
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}