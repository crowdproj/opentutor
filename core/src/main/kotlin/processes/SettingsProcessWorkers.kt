@file:OptIn(ExperimentalTime::class)

package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.core.mappers.fromDbUserDetails
import com.gitlab.sszuev.flashcards.core.mappers.toDbUserDetails
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.repositories.DbUser
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.core.processes.SettingsProcessWorkers")

fun ChainDSL<SettingsContext>.processGetSettings() = worker {
    this.name = "process get-settings request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        val dbUser = this.repositories.userRepository.getOrCreateUser(userId)
        val settings = fromDbUserDetails(dbUser.details, this.config)
        this.responseSettingsEntity = settings
        this.status = AppStatus.RUN
    }
    onException {
        fail(
            runError(
                operation = SettingsOperation.GET_SETTINGS,
                fieldName = this.normalizedRequestAppAuthId.toFieldName(),
                description = "exception",
                exception = it,
            )
        )
    }
}

fun ChainDSL<SettingsContext>.processUpdateSettings() = worker {
    this.name = "process update-settings request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        val foundUser = this.repositories.userRepository.getOrCreateUser(userId)
        val settings = this.requestSettingsEntity
        if (logger.isDebugEnabled) {
            logger.debug("Updated settings: $settings")
        }
        val dbUser = DbUser(userId.asString(), details = foundUser.details + settings.toDbUserDetails())
        this.repositories.userRepository.putUser(userId, dbUser)
        this.status = AppStatus.RUN
    }
    onException {
        fail(
            runError(
                operation = SettingsOperation.UPDATE_SETTINGS,
                fieldName = this.normalizedRequestAppAuthId.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}