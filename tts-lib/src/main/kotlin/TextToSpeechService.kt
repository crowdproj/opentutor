package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.speaker.impl.CombinedTextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.impl.EspeakNgTestToSpeechService
import com.gitlab.sszuev.flashcards.speaker.impl.LocalTextToSpeechService
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.speaker.TextToSpeechService")

/**
 * A common interface that provides access to audio resources.
 */
interface TextToSpeechService {

    /**
     * Returns bytes with audio stream by the specified resource identifier.
     * @param [id][String] the resource path identifier
     * @return [ByteArray] or `null` if resource is not found
     * @throws Exception something is wrong
     */
    suspend fun getResource(id: String, vararg args: String): ByteArray?

    /**
     * Answers `true` if resource can be provided.
     * @param [id][String] the resource path identifier
     * @return [Boolean]
     */
    suspend fun containsResource(id: String): Boolean {
        return true
    }
}

/**
 * Creates a [TextToSpeechService].
 */
fun createTTSService(): TextToSpeechService {
    return if (TTSSettings.ttsServiceVoicerssKey.isNotBlank() && TTSSettings.ttsServiceVoicerssKey != "secret") {
        logger.info("::[TTS-SERVICE] init voicerss service")
        CombinedTextToSpeechService()
    } else if (EspeakNgTestToSpeechService.isEspeakNgAvailable()) {
        logger.info("::[TTS-SERVICE] init espeak-ng service")
        EspeakNgTestToSpeechService()
    } else if (TTSSettings.localDataDirectory.isNotBlank()) {
        logger.info("::[TTS-SERVICE] init local (test) service (data dir = ${TTSSettings.localDataDirectory})")
        LocalTextToSpeechService.load(TTSSettings.localDataDirectory)
    } else {
        throw IllegalStateException("Unable to init TTS service")
    }
}

/**
 * @param [resourceId] of resource
 * @return lang-tag to word pair
 */
internal fun toResourcePath(resourceId: String): Pair<String, String>? {
    if (!resourceId.contains(":")) {
        return null
    }
    val lang = resourceId.substringBefore(":").trim()
    if (lang.isBlank()) {
        return null
    }
    val word = resourceId.substringAfter(":").trim()
    if (word.isBlank()) {
        return null
    }
    return lang to word
}