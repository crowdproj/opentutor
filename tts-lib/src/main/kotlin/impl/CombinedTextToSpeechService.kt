package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.toResourcePath

class CombinedTextToSpeechService(
    resourceIdMapper: (String) -> Pair<String, String>? = { toResourcePath(it) },
    config: TTSConfig = TTSConfig(),
) : TextToSpeechService {

    private val voicerssTextToSpeechService =
        VoicerssTextToSpeechService(resourceIdMapper = resourceIdMapper, config = config)
    private val espeakNgTestToSpeechService =
        EspeakNgTestToSpeechService(resourceIdMapper = resourceIdMapper, config = config)
    private val cache = CaffeineResourceCache()


    override suspend fun getResource(id: String, vararg args: String): ByteArray? {
        var res = cache.get(id)
        if (res != null) {
            return res
        }
        res = voicerssTextToSpeechService.getResource(id, *args)
        if (res != null) {
            cache.put(id, res)
            return res
        }
        return espeakNgTestToSpeechService.getResource(id, *args)
    }

    override suspend fun containsResource(id: String): Boolean {
        if (cache.get(id) != null) {
            return true
        }
        return voicerssTextToSpeechService.containsResource(id) || espeakNgTestToSpeechService.containsResource(id)
    }


}