package com.gitlab.sszuev.flashcards.speaker.impl

/**
 * A common interface that provides access to resources by the text.
 */
interface ResourceStore {

    /**
     * Returns a resource identifier that corresponds to the given [word] and [options].
     * The [options] array may contain hints to help identify the resource, for example, it may contain a part of speech.
     *
     * @param [word] path identifier
     * @param [options] an array, possible empty
     * @return [String] or `null` in case no resource found
     */
    fun getResourcePath(word: String, vararg options: String): String?

    /**
     * Returns resource with audio bytes by the specified resource identifier.
     *
     * @param [path] the resource path identifier
     * @return [ByteArray]
     * @throws RuntimeException in case no resource found or any other error occurred
     */
    fun getResource(path: String): ByteArray
}