package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.services.ServicesConfig
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
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
        val url = "nats://${ServicesConfig.natsHost}:${ServicesConfig.natsPort}"
        if (logger.isDebugEnabled) {
            logger.debug("NATS URL:: $url")
        }
        val options = Options.Builder()
            .server(url)
            .maxReconnects(-1)
            .reconnectWait(Duration.ofSeconds(2))
            .pingInterval(Duration.ofSeconds(10))
            .connectionListener { conn, event ->
                logger.warn("NATS event: $event | Status: ${conn.status}")
            }
            .build()
        return Nats.connect(options)
    }
}