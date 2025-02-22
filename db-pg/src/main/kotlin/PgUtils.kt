package com.gitlab.sszuev.flashcards.dbpg

import kotlinx.datetime.Instant

fun Instant.truncateToMills(): Instant = Instant.fromEpochMilliseconds(toEpochMilliseconds())