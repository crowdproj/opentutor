package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class DbDictionaryRepositoryTest {
    abstract val repository: DbDictionaryRepository

    @Test
    fun `test get all dictionaries success`() {
        val res = repository.getAllDictionaries()
        Assertions.assertEquals(2, res.dictionaries.size)
        Assertions.assertTrue(res.errors.isEmpty())

        val businessDictionary = res.dictionaries[0]
        Assertions.assertEquals(DictionaryId("1"), businessDictionary.dictionaryId)
        Assertions.assertEquals("Business vocabulary (Job)", businessDictionary.name)
        Assertions.assertEquals(LangId("EN"), businessDictionary.sourceLangId)
        Assertions.assertEquals(LangId("RU"), businessDictionary.targetLangId)

        val weatherDictionary = res.dictionaries[1]
        Assertions.assertEquals(DictionaryId("2"), weatherDictionary.dictionaryId)
        Assertions.assertEquals("Weather", weatherDictionary.name)
        Assertions.assertEquals(LangId("EN"), weatherDictionary.sourceLangId)
        Assertions.assertEquals(LangId("RU"), weatherDictionary.targetLangId)
    }
}