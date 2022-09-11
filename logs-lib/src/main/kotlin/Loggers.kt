package com.gitlab.sszuev.flashcards.logslib

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
fun noOpLogger(): ExtLogger = NoOpLogger

fun logger(clazz: Class<out Any>): ExtLogger = logger(logger = LoggerFactory.getLogger(clazz) as Logger)

fun logger(logger: Logger): ExtLogger = LogbackWrapper(
    logger = logger,
    loggerId = logger.name,
)