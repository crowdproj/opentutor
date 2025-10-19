package com.gitlab.sszuev.flashcards

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.gitlab.sszuev.flashcards.config.KeycloakConfig
import com.gitlab.sszuev.flashcards.config.RunConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.Base64

private val testKeycloakConfig = KeycloakConfig(
    authorizeAddress = "http://test-keycloak-server.ex",
    accessTokenAddress = "http://test-keycloak-server.ex",
    redirectAddress = "http://test-keycloak-server.ex",
    clientId = "test-client",
    realm = "test-realm",
)
private const val testSecret = "testSecret"
private const val testIssuer = "testIssuer"
private const val testAudience = "testAudience"

private val testRunConfig = RunConfig(auth = "", mode = RunConfig.Mode.TEST)

private val testJwtVerifier: JWTVerifier = JWT.require(
    Algorithm.HMAC256(Base64.getUrlDecoder().decode(testSecret))
).withIssuer(testIssuer)
    .withClaimPresence("sub")
    .build()

fun testSecuredApp(
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    testApplication {
        environment {
            config = ApplicationConfig("application-test.conf")
        }
        application {
            module(
                keycloakConfig = testKeycloakConfig,
                runConfig = testRunConfig,
                oauthJwtVerifier = testJwtVerifier,
                keycloakSecret = testSecret,
            )
        }
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
) {
    val token = JWT.create()
        .withSubject(id)
        .withAudience(testAudience)
        .withIssuer(testIssuer)
        .sign(Algorithm.HMAC256(Base64.getUrlDecoder().decode(testSecret)))
    header(HttpHeaders.Authorization, "Bearer $token")
}

fun ApplicationTestBuilder.testClient() = createClient {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter())
    }
    install(Logging) {
        level = LogLevel.BODY
        logger = object : Logger {
            override fun log(message: String) = println("CLIENT> $message")
        }
    }
}