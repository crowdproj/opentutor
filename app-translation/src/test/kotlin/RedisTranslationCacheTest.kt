package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import io.lettuce.core.api.sync.RedisCommands
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
internal class RedisTranslationCacheTest {

    companion object {
        private val redisContainer = GenericContainer<Nothing>(DockerImageName.parse("redis:7.4.0")).apply {
            withExposedPorts(6379)
            start()
        }

        private lateinit var commands: RedisCommands<String, String>
        private lateinit var factory: TranslationRedisConnectionFactory

        @JvmStatic
        @BeforeAll
        fun setup() {
            redisContainer.start()
            val redisHost = redisContainer.host
            val redisPort = redisContainer.getMappedPort(6379)
            val redisUri = "redis://$redisHost:$redisPort/1"

            factory = TranslationRedisConnectionFactory(redisUri)
            commands = factory.stringToStringCommands

        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            factory.close()
            redisContainer.stop()
        }
    }

    @Test
    fun `test put and get roundtrip`() {
        val cache = RedisTranslationCache(commands)
        val entry = TranslationEntity(
            word = "wind",
            transcription = "wɪnd",
            partOfSpeech = "noun",
            examples = listOf(),
            translations = listOf(listOf("ветер", "порыв ветра"))
        )
        val input = listOf(entry)

        val keyWord = "wind"
        cache.put("en", "ru", keyWord, input)

        val result = cache.get("en", "ru", keyWord)

        Assertions.assertEquals(input, result)
    }

    @Test
    fun `test get returns null if key not found`() {
        val cache = RedisTranslationCache(commands)
        val result = cache.get("en", "ru", "unknown")
        Assertions.assertNull(result)
    }
}