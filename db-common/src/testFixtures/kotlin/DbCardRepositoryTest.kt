package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Note: all implementations must have the same ids in tests for the same entities to have deterministic behavior.
 */
@Suppress("FunctionName")
abstract class DbCardRepositoryTest {

    abstract val repository: DbCardRepository

    companion object {

        private fun assertEquals(expected: CardEntity, actual: CardEntity) {
            Assertions.assertNotSame(expected, actual)
            Assertions.assertEquals(expected.dictionaryId, actual.dictionaryId)
            Assertions.assertEquals(expected.answered, actual.answered)
            Assertions.assertEquals(expected.details, actual.details)
            Assertions.assertEquals(expected.examples, actual.examples)
            Assertions.assertEquals(expected.transcription, actual.transcription)
            Assertions.assertEquals(expected.translations, actual.translations)
            Assertions.assertEquals(expected.partOfSpeech, actual.partOfSpeech)
            Assertions.assertEquals(expected.word, actual.word)
        }

        private fun assertSingleError(res: CardEntityDbResponse, field: String, op: String): AppError {
            Assertions.assertEquals(1, res.errors.size)
            val error = res.errors[0]
            Assertions.assertEquals("database::$op", error.code)
            Assertions.assertEquals(field, error.field)
            Assertions.assertEquals("database", error.group)
            Assertions.assertNull(error.exception)
            return error
        }
    }

    @Test
    fun `test get card error unknown card`() {
        val id = CardId("42000")
        val res = repository.getCard(id)
        Assertions.assertEquals(CardEntity.EMPTY, res.card)
        Assertions.assertEquals(1, res.errors.size)
        val error = res.errors[0]
        Assertions.assertEquals("database::getCard", error.code)
        Assertions.assertEquals(id.asString(), error.field)
        Assertions.assertEquals("database", error.group)
        Assertions.assertEquals(
            """Error while getCard: card with id="${id.asString()}" not found""",
            error.message
        )
        Assertions.assertNull(error.exception)
    }

    @Test
    fun `test get all cards success`() {
        // Business dictionary
        val res1 = repository.getAllCards(DictionaryId("1"))
        Assertions.assertEquals(242, res1.cards.size)
        Assertions.assertEquals(0, res1.errors.size)

        // Weather dictionary
        val res2 = repository.getAllCards(DictionaryId("2"))
        Assertions.assertEquals(65, res2.cards.size)
        Assertions.assertEquals(0, res2.errors.size)
    }

    @Test
    fun `test get all cards error unknown dictionary`() {
        val dictionaryId = "42"
        val res = repository.getAllCards(DictionaryId(dictionaryId))
        Assertions.assertEquals(0, res.cards.size)
        Assertions.assertEquals(1, res.errors.size)
        val error = res.errors[0]
        Assertions.assertEquals("database::getAllCards", error.code)
        Assertions.assertEquals(dictionaryId, error.field)
        Assertions.assertEquals("database", error.group)
        Assertions.assertEquals(
            """Error while getAllCards: dictionary with id="$dictionaryId" not found""",
            error.message
        )
        Assertions.assertNull(error.exception)
    }

    @Test
    fun `test create card success`() {
        val request = CardEntity(
            dictionaryId = DictionaryId("2"),
            word = "murky",
            transcription = "ˈmɜːkɪ",
            partOfSpeech = "adjective",
            translations = listOf(listOf("темный"), listOf("пасмурный")),
            examples = listOf("Well, that's a murky issue, isn't it?"),
            answered = 42,
            details = mapOf(Stage.OPTIONS to 0),
        )
        val res = repository.createCard(request)
        Assertions.assertEquals(0, res.errors.size)
        Assertions.assertEquals(request.dictionaryId, res.card.dictionaryId)
        Assertions.assertEquals(request.word, res.card.word)
        Assertions.assertEquals(request.transcription, res.card.transcription)
        Assertions.assertEquals(request.partOfSpeech, res.card.partOfSpeech)
        Assertions.assertEquals(request.translations, res.card.translations)
        Assertions.assertEquals(request.examples, res.card.examples)
        Assertions.assertEquals(request.answered, res.card.answered)
        Assertions.assertEquals(request.details, res.card.details)
        Assertions.assertNotEquals(request.cardId, res.card.cardId)
        Assertions.assertTrue(res.card.cardId.asString().matches("\\d+".toRegex()))
    }

    @Test
    fun `test create card error unknown dictionary`() {
        val dictionaryId = "42"
        val request = CardEntity(
            dictionaryId = DictionaryId(dictionaryId),
            word = "xxx",
            transcription = "xxx",
            translations = listOf(listOf("xxx")),
            answered = 42,
        )
        val res = repository.createCard(request)
        Assertions.assertEquals(CardEntity.EMPTY, res.card)
        val error = assertSingleError(res, dictionaryId, "createCard")
        Assertions.assertEquals(
            """Error while createCard: dictionary with id="$dictionaryId" not found""",
            error.message
        )
        Assertions.assertNull(error.exception)
    }

    @Test
    fun `test search cards random with unknown`() {
        val filter = CardFilter(
            dictionaryIds = listOf(DictionaryId("1"), DictionaryId("2"), DictionaryId("3")),
            withUnknown = true,
            random = true,
            length = 300,
        )
        val res1 = repository.searchCard(filter)
        val res2 = repository.searchCard(filter)

        Assertions.assertEquals(0, res1.errors.size)
        Assertions.assertEquals(0, res2.errors.size)
        Assertions.assertEquals(300, res1.cards.size)
        Assertions.assertEquals(300, res2.cards.size)
        Assertions.assertNotEquals(res1, res2)
        Assertions.assertEquals(setOf(DictionaryId("1"), DictionaryId("2")), res1.cards.map { it.dictionaryId }.toSet())
        Assertions.assertEquals(setOf(DictionaryId("1"), DictionaryId("2")), res2.cards.map { it.dictionaryId }.toSet())
    }

    @Test
    fun `test get card & update card success`() {
        val request = CardEntity(
            cardId = CardId("244"),
            dictionaryId = DictionaryId("2"),
            word = "climate",
            transcription = "ˈklaɪmɪt",
            translations = listOf(listOf("к-климат")),
            examples = listOf("Create a climate of fear, and it's easy to keep the borders closed."),
            answered = 42,
            details = mapOf(Stage.SELF_TEST to 3),
            partOfSpeech = "Unknown"
        )
        val prev = repository.getCard(request.cardId).card
        Assertions.assertEquals(request.dictionaryId, prev.dictionaryId)
        Assertions.assertNotEquals(request.answered, prev.answered)
        Assertions.assertNotEquals(request.details, prev.details)
        Assertions.assertNotEquals(request.examples, prev.examples)
        Assertions.assertNotEquals(request.transcription, prev.transcription)
        Assertions.assertNotEquals(request.translations, prev.translations)
        Assertions.assertNotEquals(request.partOfSpeech, prev.partOfSpeech)
        Assertions.assertNotEquals(request.word, prev.word)

        val res = repository.updateCard(request)
        Assertions.assertEquals(0, res.errors.size) { "Has errors: ${res.errors}" }
        val updated = repository.updateCard(request).card
        assertEquals(request, updated)

        val now = repository.getCard(request.cardId).card
        assertEquals(request, now)
    }

    @Test
    fun `test update card error unknown card`() {
        val id = CardId("4200")
        val request = CardEntity.EMPTY.copy(
            cardId = id, dictionaryId = DictionaryId("2"), translations = listOf(listOf("xxx"))
        )
        val res = repository.updateCard(request)
        val error = assertSingleError(res, id.asString(), "updateCard")
        Assertions.assertEquals(
            """Error while updateCard: card with id="${id.asString()}" not found""",
            error.message
        )
    }

    @Test
    fun `test update card error unknown dictionary`() {
        val cardId = CardId("42")
        val dictionaryId = DictionaryId("4200")
        val request = CardEntity.EMPTY.copy(
            cardId = cardId, dictionaryId = dictionaryId, translations = listOf(listOf("xxx"))
        )
        val res = repository.updateCard(request)
        val error = assertSingleError(res, dictionaryId.asString(), "updateCard")
        Assertions.assertEquals(
            """Error while updateCard: dictionary with id="${dictionaryId.asString()}" not found""",
            error.message
        )
    }
}