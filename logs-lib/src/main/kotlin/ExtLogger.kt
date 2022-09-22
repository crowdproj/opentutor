package com.gitlab.sszuev.flashcards.logslib

import org.slf4j.Marker
import org.slf4j.event.Level

interface ExtLogger {
    val isDebugEnabled: Boolean

    fun error(
        msg: String = "",
        throwable: Throwable? = null,
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) = log(msg = msg, level = Level.ERROR, throwable = throwable, data = data, args = args)

    fun warn(
        msg: String = "",
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) = log(msg = msg, level = Level.WARN, data = data, args = args)

    fun info(
        msg: String = "",
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) = log(msg = msg, level = Level.INFO, data = data, args = args)

    fun debug(
        msg: String = "",
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) = log(msg = msg, level = Level.DEBUG, data = data, args = args)

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