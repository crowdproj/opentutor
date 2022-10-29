package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteCardDbResponse
import org.junit.jupiter.api.*

/**
 * Note: all implementations must have the same ids in tests for the same entities to have deterministic behavior.
 */
@Suppress("FunctionName")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class DbCardRepositoryTest {

    abstract val repository: DbCardRepository

    companion object {

        private val weatherCardEntity = CardEntity(
            cardId = CardId("244"),
            dictionaryId = DictionaryId("2"),
            word = "weather",
            transcription = "'weðə",
            partOfSpeech = "NOUN",
            answered = null,
            translations = listOf(listOf("погода")),
            examples = listOf(
                "weather forecast -- прогноз погоды",
                "weather bureau -- бюро погоды",
                "nasty weather -- ненастная погода",
                "spell of cold weather -- похолодание"
            ),
            details = emptyMap(),
        )

        private val climateCardEntity = CardEntity(
            cardId = weatherCardEntity.cardId,
            dictionaryId = DictionaryId("2"),
            word = "climate",
            transcription = "ˈklaɪmɪt",
            details = mapOf(Stage.SELF_TEST to 3),
            partOfSpeech = "Unknown",
            answered = null,
            translations = listOf(listOf("климат", "атмосфера", "обстановка"), listOf("климатические условия")),
            examples = listOf(
                "Create a climate of fear, and it's easy to keep the borders closed.",
                "The clock of climate change is ticking in these magnificent landscapes.",
            ),
        )

        private val snowCardEntity = CardEntity(
            cardId = CardId("245"),
            dictionaryId = DictionaryId("2"),
            word = "snow",
            transcription = "snəu",
            partOfSpeech = "NOUN",
            translations = listOf(listOf("снег")),
            examples = listOf(
                "It snows. -- Идет снег.",
                "a flake of snow -- снежинка",
                "snow depth -- высота снежного покрова"
            ),
            answered = null,
            details = emptyMap(),
        )

        private val rainCardEntity = CardEntity(
            cardId = CardId("246"),
            dictionaryId = DictionaryId("2"),
            word = "rain",
            transcription = "rein",
            partOfSpeech = "NOUN",
            translations = listOf(listOf("дождь")),
            examples = listOf(
                "It rains. -- Идет дождь.",
                "heavy rain -- проливной дождь, ливень",
                "drizzling rain -- изморось",
                "torrential rain -- проливной дождь",
            ),
            answered = null,
            details = emptyMap(),
        )

        private val newMurkyCardEntity = CardEntity(
            dictionaryId = DictionaryId("2"),
            word = "murky",
            transcription = "ˈmɜːkɪ",
            partOfSpeech = "adjective",
            translations = listOf(listOf("темный"), listOf("пасмурный")),
            examples = listOf("Well, that's a murky issue, isn't it?"),
            answered = 42,
            details = mapOf(Stage.OPTIONS to 0),
        )

        private fun assertCard(expected: CardEntity, actual: CardEntity) {
            Assertions.assertEquals(expected.cardId, actual.cardId)
            assertCardNoId(expected, actual)
        }

        private fun assertCardNoId(expected: CardEntity, actual: CardEntity) {
            Assertions.assertNotSame(expected, actual)
            Assertions.assertEquals(expected.dictionaryId, actual.dictionaryId)
            Assertions.assertEquals(expected.word, actual.word)
            Assertions.assertEquals(expected.transcription, actual.transcription)
            Assertions.assertEquals(expected.partOfSpeech, actual.partOfSpeech)
            Assertions.assertEquals(expected.answered, actual.answered)
            Assertions.assertEquals(expected.details, actual.details)
            Assertions.assertEquals(expected.translations, actual.translations)
            Assertions.assertEquals(expected.examples, actual.examples)
        }

        private fun assertSingleError(res: CardDbResponse, field: String, op: String): AppError {
            Assertions.assertEquals(1, res.errors.size) { "Errors: ${res.errors}" }
            val error = res.errors[0]
            Assertions.assertEquals("database::$op", error.code) { error.toString() }
            Assertions.assertEquals(field, error.field) { error.toString() }
            Assertions.assertEquals("database", error.group) { error.toString() }
            Assertions.assertNull(error.exception) { error.toString() }
            return error
        }

        private fun assertNoErrors(res: CardDbResponse) {
            Assertions.assertEquals(0, res.errors.size) { "Has errors: ${res.errors}" }
        }

        private fun assertNoErrors(res: DeleteCardDbResponse) {
            Assertions.assertEquals(0, res.errors.size) { "Has errors: ${res.errors}" }
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

    @Order(1)
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

        Assertions.assertEquals(LangId("EN"), res1.sourceLanguageId)
        Assertions.assertEquals(LangId("EN"), res2.sourceLanguageId)
    }

    @Order(2)
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

    @Order(3)
    @Test
    fun `test create card success`() {
        val request = newMurkyCardEntity
        val res = repository.createCard(request)
        assertNoErrors(res)
        assertCardNoId(request, res.card)
        Assertions.assertTrue(res.card.cardId.asString().matches("\\d+".toRegex()))
    }

    @Order(4)
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

    @Order(5)
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

        Assertions.assertEquals(LangId("EN"), res1.sourceLanguageId)
        Assertions.assertEquals(LangId("EN"), res2.sourceLanguageId)
    }

    @Order(6)
    @Test
    fun `test get card & update card success`() {
        val expected = weatherCardEntity
        val prev = repository.getCard(expected.cardId).card
        assertCard(expected, prev)

        val request = climateCardEntity

        val res = repository.updateCard(request)
        assertNoErrors(res)
        val updated = res.card
        assertCard(request, updated)
        val now = repository.getCard(expected.cardId).card
        assertCard(request, now)
    }

    @Order(7)
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

    @Order(8)
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

    @Order(9)
    @Test
    fun `test learn cards success`() {
        val request = CardLearn(
            cardId = rainCardEntity.cardId,
            details = mapOf(Stage.SELF_TEST to 3, Stage.WRITING to 4),
        )
        val res = repository.learnCards(listOf(request))
        Assertions.assertEquals(0, res.errors.size) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(1, res.cards.size)
        val card = res.cards[0]
        val expectedCard = rainCardEntity.copy(details = request.details)
        assertCard(expected = expectedCard, actual = card)
    }

    @Order(10)
    @Test
    fun `test get card & reset card success`() {
        val request = snowCardEntity
        val prev = repository.getCard(request.cardId).card
        assertCard(request, prev)

        val expected = request.copy(answered = 0)
        val res = repository.resetCard(request.cardId)
        assertNoErrors(res)
        val updated = res.card
        assertCard(expected, updated)

        val now = repository.getCard(request.cardId).card
        assertCard(expected, now)
    }

    @Order(42)
    @Test
    fun `test get card & delete card success`() {
        val id = CardId("300")
        val res = repository.deleteCard(id)
        assertNoErrors(res)

        val now = repository.getCard(id).card
        Assertions.assertSame(CardEntity.EMPTY, now)
    }
}