package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.NONE
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository
import com.gitlab.sszuev.flashcards.repositories.DbLang
import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class DbDocumentRepositoryTest {
    abstract val documentRepository: DbDocumentRepository
    abstract val dictionaryRepository: DbDictionaryRepository
    abstract val cardRepository: DbCardRepository
    abstract val userRepository: DbUserRepository

    companion object {

        private const val USER_ID = "00000000-0000-0000-0000-000000000000"

        private val user = DbUser(USER_ID)

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
                "article",
                "numeral",
                "participle",
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

        private val dictionary = DbDictionary.NULL.copy(
            name = "test-dictionary",
            sourceLang = RU,
            targetLang = EN,
            userId = USER_ID,
            details = mapOf("A" to 42, "B" to false),
        )

        private val testCardEntity1 = DbCard.NULL.copy(
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "test",
                    partOfSpeech = "verb",
                    translations = listOf(listOf("тестировать")),
                    examples = emptyList(),
                ),
            ),
        )
        private val testCardEntity2 = DbCard.NULL.copy(
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "check",
                    partOfSpeech = "verb",
                    translations = listOf(listOf("проверять")),
                    examples = emptyList(),
                ),
            ),
        )

    }

    @Test
    fun `test save document`() {
        userRepository.createUser(user)

        val id = documentRepository.save(dictionary, listOf(testCardEntity1, testCardEntity2))
        Assertions.assertTrue(id.isNotBlank())
        val foundDictionary = dictionaryRepository.findDictionaryById(id)
        val expectedDictionary = dictionary.copy(dictionaryId = id)
        Assertions.assertEquals(expectedDictionary, foundDictionary)

        val foundCards = cardRepository.findCardsByDictionaryId(id).toList()
        val expectedCards = listOf(testCardEntity1, testCardEntity2).map { it.copy(dictionaryId = id) }
        Assertions.assertEquals(expectedCards, foundCards.map { it.copy(cardId = "", changedAt = Instant.NONE) })
    }
}