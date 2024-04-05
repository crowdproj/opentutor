package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbLang
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class DbDictionaryRepositoryTest {
    abstract val repository: DbDictionaryRepository

    companion object {

        private val EN = DbLang(
            langId = "en",
            partsOfSpeech = listOf(
                "noun",
                "verb",
                "adjective",
                "adverb",
                "pronoun",
                "preposition",
                "conjunction",
                "interjection",
                "article"
            )
        )
        private val RU = DbLang(
            langId = "ru",
            partsOfSpeech = listOf(
                "существительное",
                "прилагательное",
                "числительное",
                "местоимение",
                "глагол",
                "наречие",
                "причастие",
                "предлог",
                "союз",
                "частица",
                "междометие"
            )
        )

    }

    @Order(1)
    @Test
    fun `test get dictionary by id`() {
        val res1 = repository.findDictionaryById("2")
        Assertions.assertNotNull(res1)
        Assertions.assertEquals("Weather", res1!!.name)
        Assertions.assertEquals("42", res1.userId)
        val res2 = repository.findDictionaryById("1")
        Assertions.assertNotNull(res2)
        Assertions.assertEquals("Irregular Verbs", res2!!.name)
        Assertions.assertEquals("42", res2.userId)
    }

    @Order(1)
    @Test
    fun `test get all dictionaries by user-id success`() {
        val res = repository.findDictionariesByUserId("42").toList()
        Assertions.assertEquals(2, res.size)

        val businessDictionary = res[0]
        Assertions.assertEquals("1", businessDictionary.dictionaryId)
        Assertions.assertEquals("Irregular Verbs", businessDictionary.name)
        Assertions.assertEquals(EN, businessDictionary.sourceLang)
        Assertions.assertEquals(RU, businessDictionary.targetLang)
        val weatherDictionary = res[1]
        Assertions.assertEquals("2", weatherDictionary.dictionaryId)
        Assertions.assertEquals("Weather", weatherDictionary.name)
        Assertions.assertEquals(EN, weatherDictionary.sourceLang)
        Assertions.assertEquals(RU, weatherDictionary.targetLang)
    }

    @Order(2)
    @Test
    fun `test get all dictionaries by user-id nothing found`() {
        val res = repository.findDictionariesByUserId("42000").toList()
        Assertions.assertEquals(0, res.size)
    }

    @Order(4)
    @Test
    fun `test delete dictionary success`() {
        // Business vocabulary (Job)
        val res = repository.deleteDictionary("1")
        Assertions.assertEquals("1", res.dictionaryId)
        Assertions.assertEquals("Irregular Verbs", res.name)
        Assertions.assertNull(repository.findDictionaryById("1"))
    }

    @Order(4)
    @Test
    fun `test delete dictionary not found`() {
        val id = "42"
        Assertions.assertThrows(DbDataException::class.java) {
            repository.deleteDictionary(id)
        }
    }

    @Order(6)
    @Test
    fun `test create dictionary success`() {
        val given =
            DbDictionary(name = "test-dictionary", sourceLang = RU, targetLang = EN, userId = "42", dictionaryId = "")
        val res = repository.createDictionary(given)
        Assertions.assertEquals(given.name, res.name)
        Assertions.assertEquals(RU, res.sourceLang)
        Assertions.assertEquals(EN, res.targetLang)
        Assertions.assertFalse(res.dictionaryId.isBlank())
        Assertions.assertTrue(res.dictionaryId.matches("\\d+".toRegex()))
    }
}