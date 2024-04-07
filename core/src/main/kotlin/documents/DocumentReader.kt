package com.gitlab.sszuev.flashcards.core.documents

import java.io.InputStream

interface DocumentReader {
    /**
     * Parses the document representing the result as a dictionary.
     * The caller is responsible for closing `input`.
     *
     * @param [input][InputStream]
     * @return [DocumentDictionary]
     */
    fun parse(input: InputStream): DocumentDictionary

    /**
     * Parses the document representing the result as a dictionary.
     *
     * @param [resource][ByteArray]
     * @return [DocumentDictionary]
     */
    fun parse(resource: ByteArray): DocumentDictionary {
        return parse(resource.inputStream())
    }
}