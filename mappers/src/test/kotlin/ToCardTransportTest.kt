package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardResponse
import com.gitlab.sszuev.flashcards.mappers.v1.testutils.assertCard
import com.gitlab.sszuev.flashcards.mappers.v1.testutils.assertError
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ToCardTransportTest {

    @Test
    fun `test toGetCardResponse`() {
        val context = CardContext(
            requestId = AppRequestId("request=42"),
            operation = CardOperation.GET_CARD,
            responseCardEntity = CardEntity(
                cardId = CardId("card=42"),
                dictionaryId = DictionaryId("dictionary=42"),
                words = listOf(
                    CardWordEntity(
                        word = "xxx",
                        partOfSpeech = "pos",
                        transcription = "test",
                        translations = listOf(listOf("translation-1-1", "translation-1-2"), listOf("translation-2")),
                        examples = listOf("example1", "example2").map { CardWordExampleEntity(it) },
                    ),
                ),
                answered = 42,
                stats = mapOf(Stage.MOSAIC to 42, Stage.SELF_TEST to 21),
            ),
            errors = mutableListOf(
                AppError(
                    code = "42",
                    message = "XXX",
                    field = "YYY",
                    group = "GGG",
                )
            ),
            status = AppStatus.FAIL
        )
        val res = context.toGetCardResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.ERROR, res.result)
        Assertions.assertEquals(1, res.errors!!.size)
        assertError(context.errors[0], res.errors!![0])
        assertCard(context.responseCardEntity, res.card!!)
    }

    @Test
    fun `test toSearchCardsResponse`() {
        val context = CardContext(
            requestId = AppRequestId("request=42"),
            operation = CardOperation.SEARCH_CARDS,
            responseCardEntityList = listOf(
                CardEntity(
                    cardId = CardId("A"),
                    dictionaryId = DictionaryId("G"),
                    words = listOf(CardWordEntity(word = "xxx"))
                ),
                CardEntity(
                    cardId = CardId("B"),
                    dictionaryId = DictionaryId("F"),
                    words = listOf(CardWordEntity("yyy"))
                ),
            ),
            errors = mutableListOf(
                AppError(
                    code = "a",
                    message = "b",
                    field = "c",
                    group = "d",
                ),
                AppError(
                    code = "e",
                    message = "f",
                    field = "g",
                    group = "h",
                )
            ),
            status = AppStatus.OK
        )
        val res = context.toSearchCardsResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertEquals(2, res.errors!!.size)
        assertError(context.errors[0], res.errors!![0])
        assertError(context.errors[1], res.errors!![1])
        Assertions.assertEquals(2, res.cards!!.size)
        assertCard(context.responseCardEntityList[0], res.cards!![0])
        assertCard(context.responseCardEntityList[1], res.cards!![1])
    }

    @Test
    fun `test toGetAllCardsResponse`() {
        val context = CardContext(
            requestId = AppRequestId("request=42"),
            operation = CardOperation.GET_ALL_CARDS,
            responseCardEntityList = listOf(
                CardEntity(
                    cardId = CardId("H"),
                    dictionaryId = DictionaryId("J"),
                    words = listOf(CardWordEntity("xxx"))
                ),
                CardEntity(
                    cardId = CardId("K"),
                    dictionaryId = DictionaryId("M"),
                    words = listOf(CardWordEntity("yyy"))
                ),
            ),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = context.toGetAllCardsResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertEquals(2, res.cards!!.size)
        assertCard(context.responseCardEntityList[0], res.cards!![0])
        assertCard(context.responseCardEntityList[1], res.cards!![1])
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = ["CREATE_CARD", "UPDATE_CARD", "DELETE_CARD", "RESET_CARD"]
    )
    fun `test toCreateUpdateCardResponse`(op: CardOperation) {
        val cardEntity = CardEntity(
            cardId = CardId("A"),
            dictionaryId = DictionaryId("B"),
            words = listOf(
                CardWordEntity(
                    word = "XXX",
                    partOfSpeech = "pos",
                    transcription = "test",
                    translations = listOf(listOf("translation-1-1", "translation-1-2"), listOf("translation-2")),
                )
            ),
            changedAt = Instant.fromEpochSeconds(42_424_242_424_242L),
        )
        val context = CardContext(
            requestId = AppRequestId(op.name),
            operation = op,
            responseCardEntity = cardEntity,
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = when (op) {
            CardOperation.UPDATE_CARD -> context.toUpdateCardResponse()
            CardOperation.CREATE_CARD -> context.toCreateCardResponse()
            CardOperation.DELETE_CARD -> context.toDeleteCardResponse()
            CardOperation.RESET_CARD -> context.toResetCardResponse()
            else -> throw IllegalArgumentException()
        }

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        val card = when (op) {
            CardOperation.UPDATE_CARD -> (res as UpdateCardResponse).card!!
            CardOperation.CREATE_CARD -> (res as CreateCardResponse).card!!
            CardOperation.RESET_CARD -> (res as ResetCardResponse).card!!
            else -> {
                return
            }
        }

        assertCard(cardEntity, card)
    }

    @Test
    fun `test toLearnCardResponse`() {
        val cardEntity = CardEntity(
            cardId = CardId("A"),
            dictionaryId = DictionaryId("C"),
            words = listOf(CardWordEntity(word = "xxx")),
        )
        val context = CardContext(
            requestId = AppRequestId(CardOperation.LEARN_CARDS.name),
            operation = CardOperation.LEARN_CARDS,
            responseCardEntityList = listOf(cardEntity),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = context.toLearnCardResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertEquals(1, res.cards!!.size)
        val card = res.cards!![0]
        assertCard(cardEntity, card)
    }
}