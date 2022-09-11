package com.gitlab.sszuev.flashcards.logslib

import org.slf4j.Marker
import org.slf4j.event.Level

interface ExtLogger {

    fun info(
        msg: String = "",
        data: Any? = null,
        vararg args: Pair<String, Any>?
    )

    fun error(
        msg: String = "",
        throwable: Throwable? = null,
        data: Any? = null,
        vararg args: Pair<String, Any>?
    )

    suspend fun <R> withLogging(
        id: String = "",
        level: Level = Level.INFO,
        block: suspend () -> R,
    ): R

    fun log(
        msg: String = "",
        level: Level = Level.TRACE,
        marker: Marker = SimpleMarker("DEV"),
        throwable: Throwable? = null,
        data: Any? = null,
        vararg args: Pair<String, Any>?
    )
}