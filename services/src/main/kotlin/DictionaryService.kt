package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DictionaryContext

interface DictionaryService {

    suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext

    suspend fun createDictionary(context: DictionaryContext): DictionaryContext

    suspend fun updateDictionary(context: DictionaryContext): DictionaryContext

    suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext

    suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext

    suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext

}