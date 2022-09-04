package com.gitlab.sszuev.flashcards.logslib

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

fun logger(clazz: Class<out Any>): LogbackWrapper = logger(logger = LoggerFactory.getLogger(clazz) as Logger)

fun logger(logger: Logger): LogbackWrapper = LogbackWrapper(
    logger = logger,
    loggerId = logger.name,
)