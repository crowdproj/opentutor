package com.gitlab.sszuev.flashcards.model.common

import com.gitlab.sszuev.flashcards.AppConfig
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
private val none = Instant.fromEpochMilliseconds(Long.MIN_VALUE)

@OptIn(ExperimentalTime::class)
val Instant.Companion.NONE
    get() = none