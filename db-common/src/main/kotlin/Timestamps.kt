package com.gitlab.sszuev.flashcards

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.time.temporal.ChronoUnit

private val none = Instant.fromEpochMilliseconds(Long.MIN_VALUE)
val Instant.Companion.NONE
    get() = none

fun systemNow(): java.time.LocalDateTime =
    java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).toLocalDateTime()

fun Instant?.asJava(): java.time.LocalDateTime =
    (this ?: Instant.NONE).toJavaInstant().atOffset(java.time.ZoneOffset.UTC).toLocalDateTime()

fun java.time.LocalDateTime?.asKotlin(): Instant =
    this?.toInstant(java.time.ZoneOffset.UTC)?.toKotlinInstant() ?: Instant.NONE


