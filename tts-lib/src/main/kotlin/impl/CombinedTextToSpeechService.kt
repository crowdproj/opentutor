package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.ResourceCache
import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.toResourcePath
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(CombinedTextToSpeechService::class.java)

class CombinedTextToSpeechService(
    resourceIdMapper: (String) -> Pair<String, String>? = { toResourcePath(it) },
    config: TTSConfig = TTSConfig(),
    private val cache: ResourceCache = CaffeineResourceCache(),
    private val onGetResource: () -> Unit = {},
) : TextToSpeechService {

    private val voicerssTextToSpeechService =
        VoicerssTextToSpeechService(resourceIdMapper = resourceIdMapper, config = config)
    private val espeakNgTestToSpeechService =
        EspeakNgTestToSpeechService(resourceIdMapper = resourceIdMapper, config = config)

    override suspend fun getResource(id: String, vararg args: String): ByteArray? = try {
        var res = cache.get(id)
        if (res != null) {
            res
        } else {
            res = voicerssTextToSpeechService.getResource(id, *args)
            if (res != null) {
                onGetResource()
                cache.put(id, res)
                res
            } else {
                espeakNgTestToSpeechService.getResource(id, *args)
            }
        }
    } catch (ex: Exception) {
        logger.error(ex.message, ex)
        null
    }

    override suspend fun containsResource(id: String): Boolean {
        if (cache.get(id) != null) {
            return true
        }
        return voicerssTextToSpeechService.containsResource(id) || espeakNgTestToSpeechService.containsResource(id)
    }


}