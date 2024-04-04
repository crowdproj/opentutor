package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbLang
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@Suppress("FunctionName")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class DbDictionaryRepositoryTest {
    abstract val repository: DbDictionaryRepository

    companion object {
        private val userId = AppUserId("42")

        private val EN = LangEntity(
            LangId("en"), listOf(
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
            LangId("ru"),
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

        private val _EN = DbLang(
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
        private val _RU = DbLang(
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
        Assertions.assertEquals(_EN, businessDictionary.sourceLang)
        Assertions.assertEquals(_RU, businessDictionary.targetLang)
        val weatherDictionary = res[1]
        Assertions.assertEquals("2", weatherDictionary.dictionaryId)
        Assertions.assertEquals("Weather", weatherDictionary.name)
        Assertions.assertEquals(_EN, weatherDictionary.sourceLang)
        Assertions.assertEquals(_RU, weatherDictionary.targetLang)
    }

    @Order(2)
    @Test
    fun `test get all dictionaries by user-id nothing found`() {
        val res = repository.findDictionariesByUserId("42000").toList()
        Assertions.assertEquals(0, res.size)
    }

    @Order(3)
    @Test
    fun `test download dictionary`() {
        // Weather
        val res = repository.importDictionary(userId, DictionaryId("2"))
        Assertions.assertEquals(0, res.errors.size) { "Errors: ${res.errors}" }
        val xml = res.resource.data.toString(Charsets.UTF_16)
        Assertions.assertTrue(xml.startsWith("""<?xml version="1.0" encoding="UTF-16"?>"""))
        Assertions.assertEquals(66, xml.split("<card>").size)
        Assertions.assertTrue(xml.endsWith("</dictionary>" + System.lineSeparator()))
    }

    @Order(4)
    @Test
    fun `test delete dictionary success`() {
        // Business vocabulary (Job)
        val res = repository.removeDictionary(userId, DictionaryId("1"))
        Assertions.assertTrue(res.errors.isEmpty())
    }

    @Order(4)
    @Test
    fun `test delete dictionary not found`() {
        val id = DictionaryId("42")
        val res = repository.removeDictionary(userId, id)
        Assertions.assertEquals(1, res.errors.size)
        val error = res.errors[0]
        Assertions.assertEquals("database::removeDictionary", error.code)
        Assertions.assertEquals(id.asString(), error.field)
        Assertions.assertEquals("database", error.group)
        Assertions.assertEquals(
            """Error while removeDictionary: dictionary with id="${id.asString()}" not found""",
            error.message
        )
    }

    @Order(5)
    @Test
    fun `test upload dictionary`() {
        val txt = """
            <?xml version="1.0" encoding="UTF-16"?>
            <dictionary 
                formatVersion="6"  
                title="Test Dictionary"  
                userId="777" 
                sourceLanguageId="1033" 
                destinationLanguageId="1049" 
                targetNamespace="http://www.abbyy.com/TutorDictionary">
                <card>
                    <word>test</word>
                    <meanings>
                        <meaning partOfSpeech="3" transcription="test">
                            <statistics status="2"/>
                            <translations>
                                <word>тестировать</word>
                            </translations>
                            <examples/>
                        </meaning>
                    </meanings>
                </card>
            </dictionary>
        """.trimIndent()
        val bytes = txt.toByteArray(Charsets.UTF_16)
        val res = repository.exportDictionary(AppUserId("42"), ResourceEntity(DictionaryId.NONE, bytes))
        Assertions.assertEquals(0, res.errors.size) { "Errors: ${res.errors}" }

        Assertions.assertEquals("Test Dictionary", res.dictionary.name)
        Assertions.assertTrue(res.dictionary.dictionaryId.asString().isNotBlank())
        Assertions.assertEquals(EN, res.dictionary.sourceLang)
        Assertions.assertEquals(RU, res.dictionary.targetLang)
        Assertions.assertEquals(0, res.dictionary.totalCardsCount)
        Assertions.assertEquals(0, res.dictionary.learnedCardsCount)
    }

    @Order(6)
    @Test
    fun `test create dictionary success`() {
        val given =
            DbDictionary(name = "test-dictionary", sourceLang = _RU, targetLang = _EN, userId = "42", dictionaryId = "")
        val res = repository.createDictionary(given)
        Assertions.assertEquals(given.name, res.name)
        Assertions.assertEquals(_RU, res.sourceLang)
        Assertions.assertEquals(_EN, res.targetLang)
        Assertions.assertFalse(res.dictionaryId.isBlank())
        Assertions.assertTrue(res.dictionaryId.matches("\\d+".toRegex()))
    }
}