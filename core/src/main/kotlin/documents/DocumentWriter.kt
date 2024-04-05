package com.gitlab.sszuev.flashcards.core.documents

import java.io.ByteArrayOutputStream
import java.io.OutputStream

interface DocumentWriter {
    /**
     * Writes the dictionary into the document representing as `output`.
     * The caller is responsible for closing `output`.
     *
     * @param [dictionary][DocumentDictionary]
     * @param [output][OutputStream]
     */
    fun write(dictionary: DocumentDictionary, output: OutputStream)

    /**
     * Writes the dictionary into the document representing as `resource`.
     *
     * @param [dictionary][DocumentDictionary]
     * @return [ByteArray]
     */
    fun write(dictionary: DocumentDictionary): ByteArray {
        val res = ByteArrayOutputStream()
        write(dictionary, res)
        return res.toByteArray()
    }
}