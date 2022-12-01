package com.gitlab.sszuev.flashcards.speaker

/**
 * A common interface that provides access to audio resources.
 */
interface TextToSpeechService {

    /**
     * Returns bytes with audio stream by the specified resource identifier.
     * @param [id][String] the resource path identifier
     * @return [ByteArray]
     */
    fun getResource(id: String, vararg args: String?): ByteArray?

    /**
     * Answers `true` if resource can be provided.
     * @param [id][String] the resource path identifier
     * @return [Boolean]
     */
    fun containsResource(id: String): Boolean {
        return true
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