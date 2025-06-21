package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.impl.TranslationCache
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RedisTranslationCache::class.java)

class RedisTranslationCache(
    private val commands: RedisCommands<String, String>,
) : TranslationCache {

    val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    private fun key(sourceLang: String, targetLang: String, word: String): String {
        return "$sourceLang:$targetLang:$word"
    }

    override fun get(sourceLang: String, targetLang: String, word: String): List<TranslationEntity>? = try {
        val raw = commands.get(key(sourceLang, targetLang, word)) ?: return null
        json.decodeFromString(ListSerializer(TranslationEntity.serializer()), raw)
    } catch (ex: Exception) {
        logger.error("Redis get failed", ex)
        null
    }

    override fun put(sourceLang: String, targetLang: String, word: String, value: List<TranslationEntity>) {
        try {
            val raw = json.encodeToString(ListSerializer(TranslationEntity.serializer()), value)
            commands.set(key(sourceLang, targetLang, word), raw)
        } catch (ex: Exception) {
            logger.error("Redis put failed", ex)
        }
    }
}