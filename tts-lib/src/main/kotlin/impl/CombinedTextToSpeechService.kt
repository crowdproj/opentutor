package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.ResourceCache
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService

class CombinedTextToSpeechService(
    private val primaryTextToSpeechService: TextToSpeechService,
    private val secondaryTestToSpeechService: TextToSpeechService,
    private val cache: ResourceCache = CaffeineResourceCache(),
    private val onGetResource: () -> Unit = {},
) : TextToSpeechService {

    override suspend fun getResource(id: String, vararg args: String): ByteArray? {
        var res = cache.get(id)
        if (res != null) {
            return res
        }
        var error: Exception? = null
        res = try {
            primaryTextToSpeechService.getResource(id, *args)
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
            secondaryTestToSpeechService.getResource(id, *args)
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
        return primaryTextToSpeechService.containsResource(id) || secondaryTestToSpeechService.containsResource(id)
    }

}