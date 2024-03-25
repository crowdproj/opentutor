package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

/**
 * Note: all implementations must have the same ids in tests for the same entities to have deterministic behavior.
 */
@Suppress("FunctionName")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class DbCardRepositoryTest {

    abstract val repository: DbCardRepository

    companion object {
        private val userId = AppUserId("42")

        private val drawCardEntity = CardEntity(
            cardId = CardId("38"),
            dictionaryId = DictionaryId("1"),
            words = listOf(
                CardWordEntity(
                    word = "draw",
                    partOfSpeech = "verb",
                    translations = listOf(listOf("рисовать"), listOf("чертить")),
                    examples = emptyList(),
                ),
                CardWordEntity(
                    word = "drew",
                ),
                CardWordEntity(
                    word = "drawn",
                ),
            ),
        )
        private val forgiveCardEntity = CardEntity(
            cardId = CardId("58"),
            dictionaryId = DictionaryId("1"),
            words = listOf(
                CardWordEntity(
                    word = "forgive",
                    partOfSpeech = "verb",
                    translations = listOf(listOf("прощать")),
                    examples = emptyList(),
                ),
                CardWordEntity(
                    word = "forgave",
                ),
                CardWordEntity(
                    word = "forgiven",
                ),
            ),
        )

        private val weatherCardEntity = CardEntity(
            cardId = CardId("246"),
            dictionaryId = DictionaryId("2"),
            words = listOf(
                CardWordEntity(
                    word = "weather",
                    transcription = "'weðə",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("погода")),
                    examples = listOf(
                        CardWordExampleEntity(text = "weather forecast", translation = "прогноз погоды"),
                        CardWordExampleEntity(text = "weather bureau", translation = "бюро погоды"),
                        CardWordExampleEntity(text = "nasty weather", translation = "ненастная погода"),
                        CardWordExampleEntity(text = "spell of cold weather", translation = "похолодание"),
                    ),
                ),
            ),
        )

        private val climateCardEntity = CardEntity(
            cardId = weatherCardEntity.cardId,
            dictionaryId = DictionaryId("2"),
            words = listOf(
                CardWordEntity(
                    word = "climate",
                    transcription = "ˈklaɪmɪt",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("климат", "атмосфера", "обстановка"), listOf("климатические условия")),
                    examples = listOf(
                        CardWordExampleEntity("Create a climate of fear, and it's easy to keep the borders closed."),
                        CardWordExampleEntity("The clock of climate change is ticking in these magnificent landscapes."),
                    ),
                ),
            ),
            stats = mapOf(Stage.SELF_TEST to 3),
        )

        private val snowCardEntity = CardEntity(
            cardId = CardId("247"),
            dictionaryId = DictionaryId("2"),
            words = listOf(
                CardWordEntity(
                    word = "snow",
                    transcription = "snəu",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("снег")),
                    examples = listOf(
                        CardWordExampleEntity(text = "It snows.", translation = "Идет снег."),
                        CardWordExampleEntity(text = "a flake of snow", translation = "снежинка"),
                        CardWordExampleEntity(text = "snow depth", translation = "высота снежного покрова"),
                    ),
                ),
            ),
        )

        private val newMurkyCardEntity = CardEntity(
            dictionaryId = DictionaryId("2"),
            words = listOf(
                CardWordEntity(
                    word = "murky",
                    transcription = "ˈmɜːkɪ",
                    partOfSpeech = "adjective",
                    translations = listOf(listOf("темный"), listOf("пасмурный")),
                    examples = listOf(CardWordExampleEntity("Well, that's a murky issue, isn't it?")),
                ),
            ),
            stats = mapOf(Stage.OPTIONS to 0),
            answered = 42,
        )

        @Suppress("SameParameterValue")
        private fun assertCard(
            expected: CardEntity,
            actual: CardEntity,
            ignoreChangeAt: Boolean = true,
            ignoreId: Boolean = false
        ) {
            assertNotSame(expected, actual)
            var a = actual
            if (ignoreId) {
                assertNotEquals(CardId.NONE, actual.cardId)
                a = a.copy(cardId = CardId.NONE)
            } else {
                assertEquals(expected.cardId, actual.cardId)
            }
            if (ignoreChangeAt) {
                assertNotEquals(Instant.NONE, actual.changedAt)
                a = a.copy(changedAt = Instant.NONE)
            } else {
                assertEquals(expected.changedAt, actual.changedAt)
            }
            assertNotEquals(Instant.NONE, actual.changedAt)
            assertEquals(expected, a)
        }

        private fun assertNoErrors(res: RemoveCardDbResponse) {
            assertEquals(0, res.errors.size) { "Has errors: ${res.errors}" }
        }
    }

    @Test
    fun `test get card not found`() {
        val id = CardId("42000")
        val res = repository.findCardById(id)
        assertNull(res)
    }

    @Order(1)
    @Test
    fun `test get all cards success`() {
        // Business dictionary
        val res1 = repository.findCardsByDictionaryId(DictionaryId("1")).toList()
        assertEquals(244, res1.size)
        assertEquals("1", res1.map { it.dictionaryId.asString() }.toSet().single())

        // Weather dictionary
        val res2 = repository.findCardsByDictionaryId(DictionaryId("2")).toList()
        assertEquals(65, res2.size)
        assertEquals("2", res2.map { it.dictionaryId.asString() }.toSet().single())
    }

    @Order(2)
    @Test
    fun `test get all cards error unknown dictionary`() {
        val dictionaryId = "42"
        val res = repository.findCardsByDictionaryId(DictionaryId(dictionaryId)).toList()
        assertEquals(0, res.size)
    }

    @Order(4)
    @Test
    fun `test create card error unknown dictionary`() {
        val dictionaryId = "42"
        val request = CardEntity(
            dictionaryId = DictionaryId(dictionaryId),
            words = listOf(
                CardWordEntity(
                    word = "xxx",
                    transcription = "xxx",
                    translations = listOf(listOf("xxx")),
                )
            ),
            answered = 42,
        )
        Assertions.assertThrows(DbDataException::class.java) {
            repository.createCard(request)
        }
    }

    @Order(6)
    @Test
    fun `test get card & update card success`() {
        val expected = weatherCardEntity
        val prev = repository.findCardById(expected.cardId)
        assertNotNull(prev)
        assertCard(expected = expected, actual = prev!!, ignoreChangeAt = true, ignoreId = false)

        val request = climateCardEntity

        val updated = repository.updateCard(request)
        assertCard(expected = request, actual = updated, ignoreChangeAt = true, ignoreId = false)
        val now = repository.findCardById(expected.cardId)
        assertNotNull(now)
        assertCard(expected = request, actual = now!!, ignoreChangeAt = true, ignoreId = false)
    }

    @Order(7)
    @Test
    fun `test update card error unknown card`() {
        val id = CardId("4200")
        val request = CardEntity.EMPTY.copy(
            cardId = id,
            dictionaryId = DictionaryId("2"),
            words = listOf(
                CardWordEntity(word = "XXX", translations = listOf(listOf("xxx"))),
            ),
        )
        Assertions.assertThrows(DbDataException::class.java) {
            repository.updateCard(request)
        }
    }

    @Order(8)
    @Test
    fun `test update card error unknown dictionary`() {
        val cardId = CardId("42")
        val dictionaryId = DictionaryId("4200")
        val request = CardEntity.EMPTY.copy(
            cardId = cardId,
            dictionaryId = dictionaryId,
            words = listOf(
                CardWordEntity(
                    word = "XXX",
                    translations = listOf(listOf("xxx")),
                ),
            )
        )
        Assertions.assertThrows(DbDataException::class.java) {
            repository.updateCard(request)
        }
    }

    @Order(11)
    @Test
    fun `test bulk update & find by card ids - success`() {
        val now = Clock.System.now()

        val toUpdate =
            sequenceOf(forgiveCardEntity, snowCardEntity, drawCardEntity).map { it.copy(answered = 42) }.toSet()

        val updated = repository.updateCards(toUpdate)
        assertEquals(3, updated.size)

        val res1 = updated.sortedBy { it.cardId.asLong() }
        assertCard(expected = drawCardEntity.copy(answered = 42), actual = res1[0], ignoreChangeAt = true)
        assertCard(expected = forgiveCardEntity.copy(answered = 42), actual = res1[1], ignoreChangeAt = true)
        assertCard(expected = snowCardEntity.copy(answered = 42), actual = res1[2], ignoreChangeAt = true)
        res1.forEach {
            assertTrue(it.changedAt >= now)
        }

        val res2 =
            repository.findCardsByIdIn(setOf(forgiveCardEntity.cardId, snowCardEntity.cardId, drawCardEntity.cardId))
                .sortedBy { it.cardId.asLong() }
                .toList()
        assertEquals(res1, res2)
    }

    @Order(21)
    @Test
    fun `test create card success`() {
        val request = newMurkyCardEntity
        val res = repository.createCard(request)
        assertCard(expected = request, actual = res, ignoreChangeAt = true, ignoreId = true)
        assertTrue(res.cardId.asString().matches("\\d+".toRegex()))
    }

    @Order(42)
    @Test
    fun `test get card & delete card success`() {
        val id = CardId("300")
        val res = repository.removeCard(userId, id)
        assertNoErrors(res)

        assertNull(repository.findCardById(id))
    }
}