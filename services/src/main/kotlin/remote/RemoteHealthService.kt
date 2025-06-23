package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.dbpg.PgDbHealthRepository
import com.gitlab.sszuev.flashcards.services.HealthService
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RemoteHealthService::class.java)

class RemoteHealthService(
    private val repository: PgDbHealthRepository = PgDbHealthRepository(),
) : HealthService {

    override fun ping(): Boolean {
        val dbStatus = repository.ping().also {
            if (logger.isDebugEnabled)
                logger.debug("DB ::: ${if (it) "UP" else "DOWN"}")
        }
        val natsStatus = NatsConnector.ping().also {
            if (logger.isDebugEnabled)
                logger.debug("NATS ::: ${if (it) "UP" else "DOWN"}")
        }
        return dbStatus && natsStatus
    }

}