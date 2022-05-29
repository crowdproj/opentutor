package com.gitlab.sszuev.flashcards

import kotlinx.datetime.Instant

private val none = Instant.fromEpochMilliseconds(Long.MIN_VALUE)
val Instant.Companion.NONE
    get() = none