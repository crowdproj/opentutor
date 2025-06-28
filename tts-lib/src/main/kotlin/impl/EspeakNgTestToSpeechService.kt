package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.toResourcePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * A wrapper to work with [espeak-ng](https://github.com/espeak-ng/espeak-ng).
 * To use espeak-ng via docker the image `sszuev/ubuntu:openjdk11-jre-espeak-ng` can be used.
 */
class EspeakNgTestToSpeechService(
    private val resourceIdMapper: (String) -> Pair<String, String>? = { toResourcePath(it) },
    private val config: TTSConfig = TTSConfig(),
) : TextToSpeechService {

    companion object {
        private val logger = LoggerFactory.getLogger(EspeakNgTestToSpeechService::class.java)

        private val espeakNgLanguages: Map<String, String> by lazy {
            try {
                collectLanguages()
            } catch (ex: Exception) {
                logger.error("::[ESPEAK-NG] Failed to collect espeak-ng languages: ${ex.message}")
                emptyMap()
            }
        }

        private val defaultLanguages: Map<String, String> by lazy {
            espeakNgLanguages.keys.sorted().associateBy { it.substringBefore("-") }
        }

        fun isEspeakNgAvailable(): Boolean {
            return espeakNgLanguages.isNotEmpty()
        }

        private fun languageByTag(tag: String): String? {
            val key = normalize(tag)
            return espeakNgLanguages[key] ?: defaultLanguages[key]
        }

        private fun normalize(tag: String): String {
            return tag.trim().lowercase()
        }

        private fun collectLanguages(): Map<String, String> {
            val process = ProcessBuilder("/bin/bash", "-c", """espeak-ng --voices | awk '{print $2, $4}'""").start()
            val map: Map<String, String>
            process.inputStream.bufferedReader(Charsets.UTF_8).use { br ->
                map = br.lineSequence().associate {
                    val a = it.split(" ")
                    a[0] to a[1]
                }
            }
            process.waitFor(1000, TimeUnit.MILLISECONDS)
            return map
        }
    }

    override suspend fun getResource(id: String, vararg args: String): ByteArray? = withContext(Dispatchers.IO) {
        val langToWord = resourceIdMapper(id) ?: return@withContext null
        val lang = languageByTag(langToWord.first) ?: return@withContext null
        val word = langToWord.second
        logger.info("::[ESPEAK-NG]$lang:::'$word'")
        val processBuilder =
            ProcessBuilder("/bin/bash", "-c", """espeak-ng -v $lang '$word' --stdout""")
        try {
            val s = System.currentTimeMillis()
            val process = processBuilder.start()
            val e = System.currentTimeMillis()
            val res = process.inputStream.use { it.readAllBytes() }
            val restTimeout = config.getResourceTimeoutMs - (e - s) - 100
            if (!process.waitFor(restTimeout, TimeUnit.MILLISECONDS)) {
                process.destroy()
                throw TimeoutException("Process timed out ($restTimeout ms)")
            }
            res
        } catch (ex: Exception) {
            logger.error("::[ESPEAK-NG] Can't get resource for [${lang}:$word]")
            throw ex
        }
    }

    override suspend fun containsResource(id: String): Boolean {
        if (!isEspeakNgAvailable()) {
            return false
        }
        val langToWord = resourceIdMapper(id) ?: return false
        return languageByTag(langToWord.first) != null
    }
}