package com.gitlab.sszuev.flashcards.speaker

/**
 * A common interface that provides access to audio resources.
 */
interface TextToSpeechService {

    /**
     * Returns bytes with audio stream by the specified resource identifier.
     *
     * @param [id][String] the resource path identifier
     * @return [ByteArray]
     * @throws RuntimeException in case no resource found or any other error occurred
     */
    fun getResource(id: String, vararg args: String?): ByteArray
}