package com.gitlab.sszuev.flashcards

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RootEndpointTest {
    @Test
    fun `test it is alive`() = testApplication {
        val response = client.get("/")
        Assertions.assertEquals(HttpStatusCode.NotFound, response.status)
    }
}