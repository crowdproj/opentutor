package com.gitlab.sszuev.flashcards.logslib

import org.slf4j.Marker
import org.slf4j.event.Level

object NoOpLogger : ExtLogger {
    override val isDebugEnabled: Boolean = true

    override suspend fun <R> withLogging(id: String, level: Level, block: suspend () -> R): R {
        return block()
    }

    override fun log(
        msg: String,
        level: Level,
        marker: Marker,
        throwable: Throwable?,
        data: Any?,
        vararg args: Pair<String, Any>?
    ) {
    }
}