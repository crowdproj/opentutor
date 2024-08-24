package com.gitlab.sszuev.flashcards.speaker

import io.lettuce.core.api.sync.RedisCommands
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.speaker.GetResourceListeners")

fun onGetResource(commands: RedisCommands<String, String>) = try {
    val res1 = commands.incr("words.count.total")
    var res2 = commands.incr("words.count.daily")
    val date = commands.get("words.date")?.let { Instant.parse(it) }
    val now = Instant.now()
    if (date != null && Duration.between(date, now).seconds > 24 * 60 * 60) {
        // new day
        res2 = commands.del("words.count.daily")
        commands.set("words.date", now.toString())
    }
    logger.info("Total count $res1, today's count $res2")
} catch (ex: Exception) {
    logger.error("Unexpected error on get resource", ex)
}