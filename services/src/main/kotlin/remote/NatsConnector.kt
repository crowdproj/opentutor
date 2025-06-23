package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.services.ServicesConfig
import io.nats.client.Connection
import io.nats.client.ConnectionListener
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

object NatsConnector {

    private val logger = LoggerFactory.getLogger(NatsConnector::class.java)

    private val unhealthySince = AtomicReference<Instant?>(null)

    val connection by lazy {
        createConnection().also {
            logger.info("init connection status: ${it.status}")
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
                when (event) {
                    ConnectionListener.Events.CONNECTED,
                    ConnectionListener.Events.RECONNECTED -> {
                        unhealthySince.set(null)
                    }

                    ConnectionListener.Events.DISCONNECTED -> {
                        if (conn.status == Connection.Status.RECONNECTING || conn.status == Connection.Status.DISCONNECTED) {
                            unhealthySince.compareAndSet(null, Instant.now())
                        }
                    }

                    else -> {
                        /* no-op */
                    }
                }
            }
            .build()
        return Nats.connect(options)
    }

    fun ping(): Boolean {
        return try {
            if (logger.isDebugEnabled) {
                logger.debug("Nats status = ${connection.status}")
            }
            when (connection.status) {
                Connection.Status.CONNECTED -> true

                Connection.Status.CONNECTING,
                Connection.Status.RECONNECTING -> isNatsRecentlyConnecting()

                Connection.Status.CLOSED,
                Connection.Status.DISCONNECTED -> false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isNatsRecentlyConnecting(): Boolean {
        val since = unhealthySince.updateAndGet { it ?: Instant.now() }
        return Duration.between(since, Instant.now()) < Duration.ofSeconds(30)
    }
}