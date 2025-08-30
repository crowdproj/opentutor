package com.gitlab.sszuev.flashcards.dbpg

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Instant.truncateToMills(): Instant = Instant.fromEpochMilliseconds(toEpochMilliseconds())