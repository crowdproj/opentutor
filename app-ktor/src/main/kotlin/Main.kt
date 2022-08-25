package com.gitlab.sszuev.flashcards

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.gitlab.sszuev.flashcards.api.apiV1
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.speaker.rabbitmq.RMQTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.test.NullTTSResourceRepository
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val repositories = CardRepositories(
        prodTTSClientRepository = RMQTTSResourceRepository(),
        testTTSClientRepository = NullTTSResourceRepository,
        prodCardRepository = PgDbCardRepository(),
        testCardRepository = MemDbCardRepository(),
    )
    install(Routing)

    install(CachingHeaders)
    install(DefaultHeaders)
    install(AutoHeadResponse)

    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        this.allowCredentials = true
    }

    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(SerializationFeature.INDENT_OUTPUT)
            writerWithDefaultPrettyPrinter()
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    @Suppress("OPT_IN_USAGE")
    install(Locations)

    val service = cardService(repositories)
    routing {
        apiV1(service)

        static("static") {
            resources("static")
        }
    }
}
