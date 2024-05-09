package com.gitlab.sszuev.flashcards.model.common

import com.gitlab.sszuev.flashcards.AppConfig
import kotlinx.datetime.Instant

interface AppContext {
    val operation: AppOperation
    val timestamp: Instant
    val errors: MutableList<AppError>
    val config: AppConfig

    var status: AppStatus
    var requestId: AppRequestId

    // get user:
    var requestAppAuthId: AppAuthId
    var normalizedRequestAppAuthId: AppAuthId
}

private val none = Instant.fromEpochMilliseconds(Long.MIN_VALUE)
val Instant.Companion.NONE
    get() = none