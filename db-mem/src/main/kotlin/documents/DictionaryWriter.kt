package com.gitlab.sszuev.flashcards.dbmem.documents

import com.gitlab.sszuev.flashcards.dbmem.dao.Dictionary
import java.io.ByteArrayOutputStream
import java.io.OutputStream

interface DictionaryWriter {
    /**
     * Writes the dictionary into the document representing as `output`.
     * The caller is responsible for closing `output`.
     *
     * @param [dictionary][Dictionary]
     * @param [output][OutputStream]
     */
    fun write(dictionary: Dictionary, output: OutputStream)

    /**
     * Writes the dictionary into the document representing as `resource`.
     *
     * @param [dictionary][Dictionary]
     * @return [ByteArray]
     */
    fun write(dictionary: Dictionary): ByteArray {
        val res = ByteArrayOutputStream()
        write(dictionary, res)
        return res.toByteArray()
    }
}