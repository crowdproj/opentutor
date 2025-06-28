package com.gitlab.sszuev.flashcards

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RootEndpointTest {
    @Test
    fun `test it is alive`() = testApplication {
        routing { // can't test index: it is protected through login page by keycloak, which is an external service
            get("/") {
                call.respond("OK")
            }
        }
        val response = client.get("/")
        Assertions.assertEquals(HttpStatusCode.OK, response.status)
    }
}