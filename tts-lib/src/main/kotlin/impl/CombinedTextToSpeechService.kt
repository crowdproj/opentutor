package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSResourceCache
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService

class CombinedTextToSpeechService(
    private val primaryTextToSpeechService: TextToSpeechService,
    private val secondaryTestToSpeechService: TextToSpeechService,
    private val cache: TTSResourceCache = CaffeineTTSResourceCache(),
    private val onGetResource: () -> Unit = {},
) : TextToSpeechService {

    /**
     * Retrieves a resource as a byte array based on the given identifier.
     * If the resource is available in the cache, it is returned directly.
     * Otherwise, attempts to retrieve the resource either from the primary or secondary text-to-speech service.
     * If successful, the resource is cached for future use.
     * Note that only the first successful retrieval attempt is cached,
     * since the second attempt is just a fallback, which typically performs by espeak-ng service.
     *
     * @param id the unique identifier for the resource
     * @param args additional optional arguments that may influence the retrieval of the resource
     * @return the resource as a [ByteArray], or `null` if the resource is not found
     * @throws Exception if an error occurs during resource retrieval
     */
    override suspend fun getResource(id: String, vararg args: String): ByteArray? {
        var res = cache.get(id)
        if (res != null) {
            return res
        }
        var error: Exception? = null
        res = try {
            primaryTextToSpeechService.getResource(id, *args)
        } catch (ex: Exception) {
            error = ex
            null
        }
        if (res != null) {
            onGetResource()
            cache.put(id, res)
            return res
        }
        res = try {
            secondaryTestToSpeechService.getResource(id, *args)
        } catch (ex: Exception) {
            error?.let {
                error.addSuppressed(ex)
                throw error
            }
            throw ex
        }
        return res
    }

    override suspend fun containsResource(id: String): Boolean {
        if (cache.get(id) != null) {
            return true
        }
        return primaryTextToSpeechService.containsResource(id) || secondaryTestToSpeechService.containsResource(id)
    }

}