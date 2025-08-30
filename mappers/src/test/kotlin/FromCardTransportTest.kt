package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordExampleResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnResource
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsRequest
import com.gitlab.sszuev.flashcards.mappers.v1.testutils.assertCard
import com.gitlab.sszuev.flashcards.mappers.v1.testutils.assertCardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class FromCardTransportTest {

    companion object {
        private fun assertContext(
            expectedRequestId: String,
            actual: CardContext
        ) {
            Assertions.assertEquals(expectedRequestId, actual.requestId.asString())
        }
    }

    @Test
    fun `test fromGetCardRequest`() {
        val req = GetCardRequest(
            requestId = "request=42",
            cardId = "card=42",
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "request=42",
            actual = context
        )
        Assertions.assertEquals("card=42", context.requestCardEntityId.asString())
    }

    @Test
    fun `test fromGetAllCardsRequest`() {
        val req = GetAllCardsRequest(
            requestId = "request=42",
            dictionaryId = "dictionary=42",
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "request=42",
            actual = context
        )
        Assertions.assertEquals("dictionary=42", context.requestDictionaryId.asString())
    }

    @Test
    fun `test fromSearchCardsRequest`() {
        val req = SearchCardsRequest(
            requestId = "req",
            length = 42,
            random = true,
            unknown = true,
            dictionaryIds = listOf("a", "b", "c")
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "req",
            actual = context
        )
        Assertions.assertEquals(42, context.requestCardFilter.length)
        Assertions.assertEquals(true, context.requestCardFilter.random)
        Assertions.assertEquals(true, context.requestCardFilter.onlyUnknown)
        Assertions.assertEquals(
            listOf("a", "b", "c").map { DictionaryId(it) },
            context.requestCardFilter.dictionaryIds
        )
    }

    @Test
    fun `test fromCreateCardRequest`() {
        val card = CardResource(
            dictionaryId = "42",
            cardId = "42",
            words = listOf(
                CardWordResource(
                    word = "WWW",
                    partOfSpeech = "QQQ",
                    transcription = "XXX",
                    translations = listOf(listOf("A", "B"), listOf("C")),
                    sound = null,
                    examples = listOf(
                        CardWordExampleResource(
                            example = "EEE",
                            translation = "ёёё",
                        ),
                        CardWordExampleResource(
                            example = "TTT",
                            translation = "ттт",
                        ),
                    )
                )
            ),
            answered = 42,
            stats = mapOf("options" to 42, "self-test" to 21),
            details = mapOf("VVV" to "NNN")
        )
        val req = CreateCardRequest(
            requestId = "req3",
            card = card,
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "req3",
            actual = context
        )
        assertCard(card, context.requestCardEntity)
    }

    @Test
    fun `test fromUpdateCardRequest`() {
        val card = CardResource(
            dictionaryId = "D",
            cardId = "C",
            words = listOf(
                CardWordResource(
                    word = "test",
                    partOfSpeech = "pos",
                    transcription = "test",
                    translations = listOf(listOf("translation-1-1", "translation-1-2"), listOf("translation-2")),
                    sound = null,
                    examples = listOf(
                        CardWordExampleResource(example = "example1"),
                        CardWordExampleResource(example = "example2"),
                    )
                )
            ),
            answered = 42,
            stats = mapOf("options" to 42, "self-test" to 21),
        )
        val req = CreateCardRequest(
            requestId = "req4",
            card = card
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "req4",
            actual = context
        )
        assertCard(card, context.requestCardEntity)
    }

    @Test
    fun `test fromDeleteCardRequest`() {
        val req = DeleteCardRequest(
            requestId = "req5",
            cardId = "card5",
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "req5",
            actual = context
        )
        Assertions.assertEquals("card5", context.requestCardEntityId.asString())
    }

    @Test
    fun `test fromLearnCardRequest`() {
        val req = LearnCardsRequest(
            requestId = "req6",
            cards = listOf(
                LearnResource(
                    cardId = "a",
                    details = emptyMap()
                ),
                LearnResource(
                    cardId = "b",
                    details = mapOf("self-test" to 10, "mosaic" to 8)
                ),
                LearnResource(
                    cardId = "e",
                    details = mapOf("options" to 42)
                ),
            ),
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "req6",
            actual = context
        )
        Assertions.assertEquals(3, context.requestCardLearnList.size)
        assertCardId("a", context.requestCardLearnList[0].cardId)
        assertCardId("b", context.requestCardLearnList[1].cardId)
        assertCardId("e", context.requestCardLearnList[2].cardId)
        Assertions.assertTrue(context.requestCardLearnList[0].details.isEmpty())
        Assertions.assertEquals(
            mapOf(Stage.SELF_TEST to 10L, Stage.MOSAIC to 8),
            context.requestCardLearnList[1].details
        )
        Assertions.assertEquals(mapOf(Stage.OPTIONS to 42L), context.requestCardLearnList[2].details)
    }

    @Test
    fun `test fromResetCardRequest`() {
        val req = ResetCardRequest(
            requestId = "req7",
            cardId = "card7",
        )
        val context = CardContext()
        context.fromCardTransport(req)

        assertContext(
            expectedRequestId = "req7",
            actual = context
        )
        Assertions.assertEquals("card7", context.requestCardEntityId.asString())
    }
}