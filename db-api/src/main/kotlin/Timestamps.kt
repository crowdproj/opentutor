package com.gitlab.sszuev.flashcards

import java.time.temporal.ChronoUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@OptIn(ExperimentalTime::class)
private val none = Instant.fromEpochMilliseconds(Long.MIN_VALUE)

@OptIn(ExperimentalTime::class)
val Instant.Companion.NONE
    get() = none

fun systemNow(): java.time.LocalDateTime =
    java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).toLocalDateTime()

@OptIn(ExperimentalTime::class)
fun Instant?.asJava(): java.time.LocalDateTime =
    (this ?: Instant.NONE).toJavaInstant().atOffset(java.time.ZoneOffset.UTC).toLocalDateTime()

@OptIn(ExperimentalTime::class)
fun java.time.LocalDateTime?.asKotlin(): Instant =
    this?.toInstant(java.time.ZoneOffset.UTC)?.toKotlinInstant() ?: Instant.NONE


