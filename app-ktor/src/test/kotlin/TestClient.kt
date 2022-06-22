package com.gitlab.sszuev.flashcards

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*

suspend inline fun <reified X> ApplicationTestBuilder.testPost(endpoint: String, requestBody: X): HttpResponse {
    return testClient().post(endpoint) {
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }
}

fun ApplicationTestBuilder.testClient() = createClient {
    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            enable(SerializationFeature.INDENT_OUTPUT)
            writerWithDefaultPrettyPrinter()
        }
    }
}
