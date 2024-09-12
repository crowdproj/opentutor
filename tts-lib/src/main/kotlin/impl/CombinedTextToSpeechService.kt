package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.ResourceCache
import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.toResourcePath

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

    override suspend fun getResource(id: String, vararg args: String): ByteArray? {
        var res = cache.get(id)
        if (res != null) {
            return res
        }
        var error: Exception? = null
        res = try {
            voicerssTextToSpeechService.getResource(id, *args)
        } catch (voicerssError: Exception) {
            error = voicerssError
            null
        }
        if (res != null) {
            onGetResource()
            cache.put(id, res)
            return res
        }
        return try {
            espeakNgTestToSpeechService.getResource(id, *args)
        } catch (espeakNgError: Exception) {
            error?.let {
                error.addSuppressed(espeakNgError)
                throw error
            }
            throw espeakNgError
        }
    }

    override suspend fun containsResource(id: String): Boolean {
        if (cache.get(id) != null) {
            return true
        }
        return voicerssTextToSpeechService.containsResource(id) || espeakNgTestToSpeechService.containsResource(id)
    }

}