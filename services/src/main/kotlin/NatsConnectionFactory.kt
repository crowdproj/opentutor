package com.gitlab.sszuev.flashcards.services

import io.nats.client.Connection
import io.nats.client.Nats
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

object NatsConnectionFactory {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val connection by lazy {
        createConnection().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                logger.info("Close connection on shutdown.")
                it.close()
            })
        }
    }

    private fun createConnection(): Connection {
        val url = "nats://${ServiceSettings.host}:${ServiceSettings.port}"
        return Nats.connectReconnectOnConnect(url)
    }
}
