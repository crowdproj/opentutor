package com.gitlab.sszuev.flashcards.model.common

import kotlinx.datetime.Instant

interface AppContext {
    val operation: AppOperation
    val repositories: AppRepositories
    val timestamp: Instant
    val errors: MutableList<AppError>

    var status: AppStatus
    var workMode: AppMode
    var debugCase: AppStub
    var requestId: AppRequestId

    // get user:
    var requestAppAuthId: AppAuthId
    var normalizedRequestAppAuthId: AppAuthId
    var contextUserEntity: AppUserEntity
}

private val none = Instant.fromEpochMilliseconds(Long.MIN_VALUE)
val Instant.Companion.NONE
    get() = none