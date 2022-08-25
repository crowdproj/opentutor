package com.gitlab.sszuev.flashcards.dbmem.documents

import com.gitlab.sszuev.flashcards.dbmem.dao.Dictionary
import java.io.InputStream

interface DictionaryReader {

    /**
     * Parses the document representing the result as a dictionary.
     * The caller is responsible for closing `input`.
     *
     * @param [input][InputStream]
     * @return [Dictionary]
     */
    fun parse(input: InputStream): Dictionary

    /**
     * Parses the document representing the result as a dictionary.
     *
     * @param [resource][ByteArray]
     * @return [Dictionary]
     */
    fun parse(resource: ByteArray): Dictionary {
        return parse(resource.inputStream())
    }
}