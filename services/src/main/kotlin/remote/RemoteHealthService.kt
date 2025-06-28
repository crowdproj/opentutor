package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.services.HealthService
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RemoteHealthService::class.java)

class RemoteHealthService(
) : HealthService {

    override fun ping(): Boolean = NatsConnector.ping().also {
        if (logger.isDebugEnabled) {
            logger.debug("NATS ::: ${if (it) "UP" else "DOWN"}")
        }
    }

}