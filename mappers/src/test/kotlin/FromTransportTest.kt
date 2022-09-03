package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FromTransportTest {

    @Test
    fun `test fromGetCardRequest`() {
        val req = GetCardRequest(
            requestId = "request=42",
            cardId = "card=42",
            debug = DebugResource(
                mode = RunMode.STUB,
                stub = DebugStub.SUCCESS
            ),
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.SUCCESS,
            expectedMode = AppMode.STUB,
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
            debug = DebugResource(
                mode = RunMode.TEST,
                stub = DebugStub.SUCCESS
            ),
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.SUCCESS,
            expectedMode = AppMode.TEST,
            expectedRequestId = "request=42",
            actual = context
        )
        Assertions.assertEquals("dictionary=42", context.requestDictionaryId.asString())
    }

    @Test
    fun `test fromSearchCardsRequest`() {
        val req = SearchCardsRequest(
            requestId = "req",
            debug = DebugResource(
                mode = RunMode.PROD,
                stub = DebugStub.ERROR_UNKNOWN
            ),
            length = 42,
            random = true,
            unknown = true,
            dictionaryIds = listOf("a", "b", "c")
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.UNKNOWN_ERROR,
            expectedMode = AppMode.PROD,
            expectedRequestId = "req",
            actual = context
        )
        Assertions.assertEquals(42, context.requestCardFilter.length)
        Assertions.assertEquals(true, context.requestCardFilter.random)
        Assertions.assertEquals(true, context.requestCardFilter.withUnknown)
        Assertions.assertEquals(
            listOf("a", "b", "c").map { DictionaryId(it) },
            context.requestCardFilter.dictionaryIds
        )
    }

    @Test
    fun `test fromCreateCardRequest`() {
        val card = CardResource(
            dictionaryId = "42",
            word = "test",
            partOfSpeech = "pos",
            transcription = "test",
            answered = 42,
            translations = listOf(listOf("translation-1-1", "translation-1-2"), listOf("translation-2")),
            examples = listOf("example1", "example2"),
            details = mapOf("options" to 42, "self-test" to 21)
        )
        val req = CreateCardRequest(
            requestId = "req3",
            debug = DebugResource(
                mode = RunMode.TEST,
                stub = DebugStub.SUCCESS
            ),
            card = card
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.SUCCESS,
            expectedMode = AppMode.TEST,
            expectedRequestId = "req3",
            actual = context
        )
        assertCard(card, context.requestCardEntity)
    }

    @Test
    fun `test fromUpdateCardRequest`() {
        val card = CardResource(
            cardId = "C",
            dictionaryId = "D",
            word = "test",
            partOfSpeech = "pos",
            transcription = "test",
            answered = 42,
            translations = listOf(listOf("translation-1-1", "translation-1-2"), listOf("translation-2")),
            examples = listOf("example1", "example2"),
            details = mapOf("options" to 42, "self-test" to 21)
        )
        val req = CreateCardRequest(
            requestId = "req4",
            debug = DebugResource(
                mode = RunMode.TEST,
                stub = DebugStub.ERROR_UNKNOWN
            ),
            card = card
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.UNKNOWN_ERROR,
            expectedMode = AppMode.TEST,
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
            debug = DebugResource(
                mode = RunMode.TEST,
                stub = DebugStub.ERROR_UNKNOWN
            ),
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.UNKNOWN_ERROR,
            expectedMode = AppMode.TEST,
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
            debug = DebugResource(
                mode = RunMode.TEST,
                stub = DebugStub.ERROR_UNKNOWN
            ),
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.UNKNOWN_ERROR,
            expectedMode = AppMode.TEST,
            expectedRequestId = "req6",
            actual = context
        )
        Assertions.assertEquals(3, context.requestCardLearnList.size)
        assertCardId("a", context.requestCardLearnList[0].cardId)
        assertCardId("b", context.requestCardLearnList[1].cardId)
        assertCardId("e", context.requestCardLearnList[2].cardId)
        Assertions.assertTrue(context.requestCardLearnList[0].details.isEmpty())
        Assertions.assertEquals(mapOf(Stage.SELF_TEST to 10L, Stage.MOSAIC to 8), context.requestCardLearnList[1].details)
        Assertions.assertEquals(mapOf(Stage.OPTIONS to 42L), context.requestCardLearnList[2].details)
    }

    @Test
    fun `test fromResetCardRequest`() {
        val req = ResetCardRequest(
            requestId = "req7",
            cardId = "card7",
            debug = DebugResource(
                mode = RunMode.STUB,
                stub = DebugStub.ERROR_UNKNOWN
            ),
        )
        val context = CardContext()
        context.fromTransportToRequest(req)

        assertContext(
            expectedStub = AppStub.UNKNOWN_ERROR,
            expectedMode = AppMode.STUB,
            expectedRequestId = "req7",
            actual = context
        )
        Assertions.assertEquals("card7", context.requestCardEntityId.asString())
    }

    private fun assertContext(
        expectedStub: AppStub,
        expectedMode: AppMode,
        expectedRequestId: String,
        actual: CardContext
    ) {
        Assertions.assertEquals(expectedStub, actual.debugCase)
        Assertions.assertEquals(expectedMode, actual.workMode)
        Assertions.assertEquals(expectedRequestId, actual.requestId.asString())
    }

    private fun assertCard(expected: CardResource, actual: CardEntity) {
        assertCardId(expected.cardId, actual.cardId)
        Assertions.assertEquals(expected.dictionaryId, actual.dictionaryId.asString())
        Assertions.assertEquals(expected.word, actual.word)
    }

    private fun assertCardId(expected: String?, actual: CardId) {
        Assertions.assertEquals(expected?.let { CardId(it) } ?: CardId.NONE, actual)
    }
}