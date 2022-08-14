package com.gitlab.sszuev.flashcards.dbmem

data class MemDbConfig(
    val numberOfRightAnswers: Int = MemDbSettings.numberOfRightAnswers,
    val dataLocation: String = MemDbSettings.dataLocation,
    val dataFlushPeriodInMs: Long = MemDbSettings.dataFlushPeriodInMs,
)