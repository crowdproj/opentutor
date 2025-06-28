package com.gitlab.sszuev.flashcards.nats

import com.gitlab.sszuev.flashcards.dbpg.PgDbHealthRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.nats.client.Connection
import io.nats.client.ConnectionListener
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.nats.AppRunnerKt")

private val natsUnhealthySince = AtomicReference<Instant?>(null)

suspend fun runApp(
    connectionUrl: String,
    topic: String,
    group: String,
    parallelism: Int,
    withDbHealthCheck: Boolean,
    messageHandler: MessageHandler,
    onShutdown: () -> Unit = {},
) {

    lateinit var processor: NatsServerProcessor

    val options = Options.Builder()
        .server(connectionUrl)
        .maxReconnects(-1)
        .reconnectWait(Duration.ofSeconds(2))
        .pingInterval(Duration.ofSeconds(10))
        .connectionListener { conn, event ->
            if (event == ConnectionListener.Events.CONNECTED) {
                logger.info("$event | Status: ${conn.status}")
            } else {
                logger.warn("$event | Status: ${conn.status}")
            }
            if (event == ConnectionListener.Events.RECONNECTED || event == ConnectionListener.Events.RESUBSCRIBED) {
                logger.info(">>> Triggering re-subscription to NATS topic after $event")
                processor.subscribe()
            }
            when (event) {
                ConnectionListener.Events.CONNECTED,
                ConnectionListener.Events.RECONNECTED -> {
                    natsUnhealthySince.set(null)
                }

                ConnectionListener.Events.DISCONNECTED -> {
                    if (conn.status == Connection.Status.RECONNECTING || conn.status == Connection.Status.DISCONNECTED) {
                        natsUnhealthySince.compareAndSet(null, Instant.now())
                    }
                }

                else -> {
                    /* no-op */
                }
            }
        }
        .build()

    val connection = Nats.connect(options).also {
        logger.info("Connected to NATS: $connectionUrl")
    }

    processor = NatsServerProcessor(
        topic = topic,
        group = group,
        connection = connection,
        parallelism = parallelism,
        messageHandler = messageHandler,
    )

    // Health server
    embeddedServer(Netty, port = 8080) {
        val dbHealthRepository = PgDbHealthRepository()
        routing {
            get("/health") {
                val db = if (withDbHealthCheck) {
                    dbHealthRepository.ping().also {
                        if (logger.isDebugEnabled) {
                            logger.debug("DB ::: ${if (it) "UP" else "DOWN"}")
                        }
                    }
                } else {
                    true
                }
                val nats = connection.ping().also {
                    if (logger.isDebugEnabled) {
                        logger.debug("NATS ::: ${if (it) "UP" else "DOWN"}")
                    }
                }
                db && nats
                if (db && nats) {
                    call.respondText("OK", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Unhealthy", status = HttpStatusCode.ServiceUnavailable)
                }
            }
        }
    }.start(wait = false)

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close connection on shutdown.")
        processor.close()
        onShutdown()
    })
    logger.info("Start processing.")
    processor.process().join()
}

private fun Connection.ping(): Boolean {
    return try {
        when (this.status) {
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
    val since = natsUnhealthySince.updateAndGet { it ?: Instant.now() }
    return Duration.between(since, Instant.now()) < Duration.ofSeconds(30)
}