package com.gitlab.sszuev.flashcards.repositories

/**
 * Generic (TextToSpeech) interface to provide access to audio resources (as [ByteArray]s).
 */
interface TTSResourceRepository {

    /**
     * @param lang e.g. `en`
     * @param word e.g. `test`
     * @return [ByteArray] or `null` if resource cannot be found
     * @throws RuntimeException if something is wrong, e.g., resource should be found, but it is not
     */
    suspend fun findResource(lang: String, word: String): ByteArray?
}