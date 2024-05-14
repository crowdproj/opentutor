package com.gitlab.sszuev.flashcards.speaker

import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.speaker.TTSServerMain")

fun main() {
    val config = NatsConfig()
    val processor = NatsTTSServerProcessorImpl(
        service = createTTSService(),
        topic = config.topic,
        group = config.group,
        connectionUrl = config.url,
    )
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close connection on shutdown.")
        processor.close()
    })
    logger.info("Start processing.")
    TTSServerController(processor).start()
}