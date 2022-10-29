package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import org.junit.jupiter.api.*

@Suppress("FunctionName")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class DbDictionaryRepositoryTest {
    abstract val repository: DbDictionaryRepository

    companion object {
        private val EN = LangEntity(
            LangId("EN"), listOf(
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
        private val RU = LangEntity(
            LangId("RU"),
            listOf(
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
    fun `test get all dictionaries by user-id success`() {
        val res = repository.getAllDictionaries(AppUserId("42"))
        Assertions.assertTrue(res.errors.isEmpty())

        val businessDictionary = res.dictionaries[0]
        Assertions.assertEquals(DictionaryId("1"), businessDictionary.dictionaryId)
        Assertions.assertEquals("Business vocabulary (Job)", businessDictionary.name)
        Assertions.assertEquals(EN, businessDictionary.sourceLang)
        Assertions.assertEquals(RU, businessDictionary.targetLang)
        val weatherDictionary = res.dictionaries[1]
        Assertions.assertEquals(DictionaryId("2"), weatherDictionary.dictionaryId)
        Assertions.assertEquals("Weather", weatherDictionary.name)
        Assertions.assertEquals(EN, weatherDictionary.sourceLang)
        Assertions.assertEquals(RU, weatherDictionary.targetLang)
    }

    @Order(2)
    @Test
    fun `test get all dictionaries by user-id nothing found`() {
        val res1 = repository.getAllDictionaries(AppUserId.NONE)
        Assertions.assertEquals(0, res1.dictionaries.size)
        Assertions.assertTrue(res1.errors.isEmpty())

        val res2 = repository.getAllDictionaries(AppUserId("-42"))
        Assertions.assertEquals(0, res2.dictionaries.size)
        Assertions.assertTrue(res2.errors.isEmpty())
    }

    @Order(3)
    @Test
    fun `test download dictionary`() {
        // Weather
        val res = repository.downloadDictionary(DictionaryId("2"))
        Assertions.assertTrue(res.errors.isEmpty())
        val xml = res.resource.data.toString(Charsets.UTF_16)
        Assertions.assertTrue(xml.startsWith("""<?xml version="1.0" encoding="UTF-16" standalone="yes"?>"""))
        Assertions.assertEquals(66, xml.split("<card>").size)
        Assertions.assertTrue(xml.substring(29000).endsWith("</dictionary>\n"))
    }

    @Order(42)
    @Test
    fun `test delete dictionary success`() {
        // Business vocabulary (Job)
        val res = repository.deleteDictionary(DictionaryId("1"))
        Assertions.assertTrue(res.errors.isEmpty())
    }

    @Order(42)
    @Test
    fun `test delete dictionary not found`() {
        val id = DictionaryId("42")
        val res = repository.deleteDictionary(id)
        Assertions.assertEquals(1, res.errors.size)
        val error = res.errors[0]
        Assertions.assertEquals("database::deleteDictionary", error.code)
        Assertions.assertEquals(id.asString(), error.field)
        Assertions.assertEquals("database", error.group)
        Assertions.assertEquals(
            """Error while deleteDictionary: dictionary with id="${id.asString()}" not found""",
            error.message
        )
    }

}