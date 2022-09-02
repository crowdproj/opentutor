package com.gitlab.sszuev.flashcards.logslib

import ch.qos.logback.classic.Logger
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.Marker
import org.slf4j.event.KeyValuePair
import org.slf4j.event.Level
import org.slf4j.event.LoggingEvent

class LogbackWrapper(
    val logger: Logger,
    val loggerId: String = ""
) {

    fun info(
        msg: String = "",
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) = log(msg = msg, level = Level.INFO, data = data, args = args)

    fun error(
        msg: String = "",
        throwable: Throwable? = null,
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) = log(msg = msg, level = Level.ERROR, throwable = throwable, data = data, args = args)

    suspend fun <R> withLogging(
        id: String = "",
        level: Level = Level.INFO,
        block: suspend () -> R,
    ): R = try {
        val start = System.currentTimeMillis()
        log("$loggerId:::Start $id", level)
        block().also {
            val millis = System.currentTimeMillis() - start
            log(msg = "$loggerId:::End $id", level = level, args = arrayOf("metricHandleTimeInMillis" to millis))
        }
    } catch (ex: Exception) {
        log(msg = "$loggerId:::Fail $id", level = Level.ERROR, throwable = ex)
        throw ex
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun log(
        msg: String = "",
        level: Level = Level.TRACE,
        marker: Marker = SimpleMarker("DEV"),
        throwable: Throwable? = null,
        data: Any? = null,
        vararg args: Pair<String, Any>?
    ) {
        logger.log(object : LoggingEvent {
            private val eventThreadName = Thread.currentThread().name
            private val eventTimestamp = System.currentTimeMillis()

            override fun getThrowable() = throwable

            override fun getLevel(): Level = level

            override fun getLoggerName(): String = logger.name

            override fun getTimeStamp(): Long = eventTimestamp

            override fun getThreadName(): String = eventThreadName

            override fun getMessage(): String = msg

            override fun getMarkers(): List<Marker> = listOf(marker)

            override fun getKeyValuePairs(): List<KeyValuePair> {
                return emptyList()
            }

            override fun getArguments(): List<Any> {
                return argumentArray.toList()
            }

            override fun getArgumentArray(): Array<out Any> {
                val res = args.map { StructuredArguments.keyValue(it?.first, it?.second) }.toTypedArray()
                return if (data != null) {
                    arrayOf(res, StructuredArguments.keyValue("data", data))
                } else {
                    res
                }
            }
        })
    }
}