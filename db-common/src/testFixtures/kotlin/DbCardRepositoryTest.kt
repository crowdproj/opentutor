package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
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

        private val drawCardEntity = DbCard(
            cardId = "38",
            dictionaryId = "1",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "draw",
                    partOfSpeech = "verb",
                    translations = listOf(listOf("рисовать"), listOf("чертить")),
                    examples = emptyList(),
                ),
                DbCard.Word.NULL.copy(
                    word = "drew",
                ),
                DbCard.Word.NULL.copy(
                    word = "drawn",
                ),
            ),
        )
        private val forgiveCardEntity = DbCard(
            cardId = "58",
            dictionaryId = "1",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "forgive",
                    partOfSpeech = "verb",
                    translations = listOf(listOf("прощать")),
                    examples = emptyList(),
                ),
                DbCard.Word.NULL.copy(
                    word = "forgave",
                ),
                DbCard.Word.NULL.copy(
                    word = "forgiven",
                ),
            ),
        )

        private val weatherCardEntity = DbCard(
            cardId = "246",
            dictionaryId = "2",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word(
                    word = "weather",
                    transcription = "'weðə",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("погода")),
                    examples = listOf(
                        DbCard.Word.Example(text = "weather forecast", translation = "прогноз погоды"),
                        DbCard.Word.Example(text = "weather bureau", translation = "бюро погоды"),
                        DbCard.Word.Example(text = "nasty weather", translation = "ненастная погода"),
                        DbCard.Word.Example(text = "spell of cold weather", translation = "похолодание"),
                    ),
                ),
            ),
        )

        private val climateCardEntity = DbCard(
            cardId = weatherCardEntity.cardId,
            dictionaryId = "2",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = mapOf(Stage.SELF_TEST.name to 3),
            answered = null,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "climate",
                    transcription = "ˈklaɪmɪt",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("климат", "атмосфера", "обстановка"), listOf("климатические условия")),
                    examples = listOf(
                        DbCard.Word.Example.NULL.copy(text = "Create a climate of fear, and it's easy to keep the borders closed."),
                        DbCard.Word.Example.NULL.copy(text = "The clock of climate change is ticking in these magnificent landscapes."),
                    ),
                ),
            ),
        )

        private val snowCardEntity = DbCard(
            cardId = "247",
            dictionaryId = "2",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "snow",
                    transcription = "snəu",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("снег")),
                    examples = listOf(
                        DbCard.Word.Example(text = "It snows.", translation = "Идет снег."),
                        DbCard.Word.Example(text = "a flake of snow", translation = "снежинка"),
                        DbCard.Word.Example(text = "snow depth", translation = "высота снежного покрова"),
                    ),
                ),
            ),
        )

        private val newMurkyCardEntity = DbCard(
            cardId = "",
            dictionaryId = "2",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = mapOf(Stage.OPTIONS.name to 0),
            answered = 42,
            words = listOf(
                DbCard.Word.NULL.copy(
                    word = "murky",
                    transcription = "ˈmɜːkɪ",
                    partOfSpeech = "adjective",
                    translations = listOf(listOf("темный"), listOf("пасмурный")),
                    examples = listOf(DbCard.Word.Example.NULL.copy(text = "Well, that's a murky issue, isn't it?")),
                ),
            ),
        )

        @Suppress("SameParameterValue")
        private fun assertCard(
            expected: DbCard,
            actual: DbCard,
            ignoreChangeAt: Boolean = true,
            ignoreId: Boolean = false
        ) {
            assertNotSame(expected, actual)
            var a = actual
            if (ignoreId) {
                assertNotEquals("", actual.cardId)
                a = a.copy(cardId = "")
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
    }

    @Test
    fun `test get card not found`() {
        val id = "42000"
        val res = repository.findCardById(id)
        assertNull(res)
    }

    @Order(1)
    @Test
    fun `test get all cards success`() {
        // Business dictionary
        val res1 = repository.findCardsByDictionaryId("1").toList()
        assertEquals(244, res1.size)
        assertEquals("1", res1.map { it.dictionaryId }.toSet().single())

        // Weather dictionary
        val res2 = repository.findCardsByDictionaryId("2").toList()
        assertEquals(65, res2.size)
        assertEquals("2", res2.map { it.dictionaryId }.toSet().single())
    }

    @Order(2)
    @Test
    fun `test get all cards error unknown dictionary`() {
        val dictionaryId = "42"
        val res = repository.findCardsByDictionaryId(dictionaryId).toList()
        assertEquals(0, res.size)
    }

    @Order(4)
    @Test
    fun `test create card error unknown dictionary`() {
        val dictionaryId = "42"
        val request = DbCard.NULL.copy(
            dictionaryId = dictionaryId,
            words = listOf(
                DbCard.Word.NULL.copy(
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
        val id = "4200"
        val request = DbCard.NULL.copy(
            cardId = id,
            dictionaryId = "2",
            words = listOf(
                DbCard.Word.NULL.copy(word = "XXX", translations = listOf(listOf("xxx"))),
            ),
        )
        Assertions.assertThrows(DbDataException::class.java) {
            repository.updateCard(request)
        }
    }

    @Order(8)
    @Test
    fun `test update card error unknown dictionary`() {
        val cardId = "42"
        val dictionaryId = "4200"
        val request = DbCard.NULL.copy(
            cardId = cardId,
            dictionaryId = dictionaryId,
            words = listOf(
                DbCard.Word.NULL.copy(
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

        val res1 = updated.sortedBy { it.cardId.toLong() }
        assertCard(expected = drawCardEntity.copy(answered = 42), actual = res1[0], ignoreChangeAt = true)
        assertCard(expected = forgiveCardEntity.copy(answered = 42), actual = res1[1], ignoreChangeAt = true)
        assertCard(expected = snowCardEntity.copy(answered = 42), actual = res1[2], ignoreChangeAt = true)
        res1.forEach {
            assertTrue(it.changedAt >= now)
        }

        val res2 =
            repository.findCardsByIdIn(setOf(forgiveCardEntity.cardId, snowCardEntity.cardId, drawCardEntity.cardId))
                .sortedBy { it.cardId.toLong() }
                .toList()
        assertEquals(res1, res2)
    }

    @Order(21)
    @Test
    fun `test create card success`() {
        val request = newMurkyCardEntity
        val res = repository.createCard(request)
        assertCard(expected = request, actual = res, ignoreChangeAt = true, ignoreId = true)
        assertTrue(res.cardId.matches("\\d+".toRegex()))
    }

    @Order(42)
    @Test
    fun `test get card & delete card success`() {
        val id = "300"
        val res = repository.deleteCard(id)
        assertEquals(id, res.cardId)

        assertNull(repository.findCardById(id))
    }
}