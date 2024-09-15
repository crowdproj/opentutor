package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.core.mappers.fromDbUserDetails
import com.gitlab.sszuev.flashcards.core.mappers.toDbUserDetails
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.repositories.DbUser

fun ChainDSL<SettingsContext>.processGetSettings() = worker {
    this.name = "process get-settings request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        this.repositories.userRepository.createUserIfAbsent(userId)
        val dbUser = checkNotNull(this.repositories.userRepository.findByUserId(userId.asString()))
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
                exception = it
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
        this.repositories.userRepository.createUserIfAbsent(userId)
        val settings = this.requestSettingsEntity
        val dbUser = DbUser(userId.asString(), details = settings.toDbUserDetails())
        this.repositories.userRepository.updateUser(dbUser)
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