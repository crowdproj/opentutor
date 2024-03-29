package com.gitlab.sszuev.flashcards

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gitlab.sszuev.flashcards.config.KeycloakConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.Base64

val testKeycloakConfig = KeycloakConfig(
    address = "http://test-keycloak-server.ex",
    clientId = "test-client",
    secret = "test-secret",
    issuer = "test-issuer",
    audience = "test-audience",
    realm = "test-realm",
)

@OptIn(KtorExperimentalLocationsAPI::class)
fun testSecuredApp(
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    testApplication {
        application { module(keycloakConfig = testKeycloakConfig) }
        block()
    }
}

suspend inline fun <reified X> ApplicationTestBuilder.testPost(endpoint: String, requestBody: X): HttpResponse {
    return testClient().post(endpoint) {
        contentType(ContentType.Application.Json)
        setBody(requestBody)
        auth()
    }
}

fun HttpRequestBuilder.auth(
    id: String = "c9a414f5-3f75-4494-b664-f4c8b33ff4e6",
    conf: KeycloakConfig = testKeycloakConfig,
) {
    val token = JWT.create()
        .withSubject(id)
        .withAudience(conf.audience)
        .withIssuer(conf.issuer)
        .sign(Algorithm.HMAC256(Base64.getUrlDecoder().decode(conf.secret)))
    header(HttpHeaders.Authorization, "Bearer $token")
}

fun ApplicationTestBuilder.testClient() = createClient {
    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(SerializationFeature.INDENT_OUTPUT)
            writerWithDefaultPrettyPrinter()
            registerModule(JavaTimeModule())
        }
    }
}
