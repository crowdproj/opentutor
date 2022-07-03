package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ToTransportTest {

    @Test
    fun `test toGetCardResponse`() {
        val context = CardContext(
            requestId = AppRequestId("request=42"),
            operation = CardOperation.GET_CARD,
            responseCardEntity = CardEntity(
                cardId = CardId("card=42"),
                dictionaryId = DictionaryId("dictionary=42"),
                word = "xxx"
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
    fun `test toGetCardsResponse`() {
        val context = CardContext(
            requestId = AppRequestId("request=42"),
            operation = CardOperation.SEARCH_CARDS,
            responseCardEntityList = listOf(
                CardEntity(
                    cardId = CardId("A"),
                    dictionaryId = DictionaryId("G"),
                    word = "xxx"
                ),
                CardEntity(
                    cardId = CardId("B"),
                    dictionaryId = DictionaryId("F"),
                    word = "yyy"
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
        val res = context.toGetCardsResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertEquals(2, res.errors!!.size)
        assertError(context.errors[0], res.errors!![0])
        assertError(context.errors[1], res.errors!![1])
        Assertions.assertEquals(2, res.cards!!.size)
        assertCard(context.responseCardEntityList[0], res.cards!![0])
        assertCard(context.responseCardEntityList[1], res.cards!![1])
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = ["CREATE_CARD", "UPDATE_CARD", "DELETE_CARD", "LEARN_CARD", "RESET_CARD"]
    )
    fun `test toCreateUpdateCardResponse`(op: CardOperation) {
        val context = CardContext(
            requestId = AppRequestId(op.name),
            operation = op,
            responseCardEntity =
            CardEntity(
                cardId = CardId("A"),
                dictionaryId = DictionaryId("G"),
                word = "xxx"
            ),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = when (op) {
            CardOperation.UPDATE_CARD -> context.toUpdateCardResponse()
            CardOperation.CREATE_CARD -> context.toCreateCardResponse()
            CardOperation.DELETE_CARD -> context.toDeleteCardResponse()
            CardOperation.LEARN_CARD -> context.toLearnCardResponse()
            CardOperation.RESET_CARD-> context.toResetCardResponse()
            else -> throw IllegalArgumentException()
        }

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
    }

    private fun assertError(expected: AppError, actual: ErrorResource) {
        Assertions.assertEquals(expected.code, actual.code)
        Assertions.assertEquals(expected.message, actual.message)
        Assertions.assertEquals(expected.field, actual.field)
        Assertions.assertEquals(expected.group, actual.group)
    }

    private fun assertCard(expected: CardEntity, actual: CardResource) {
        Assertions.assertEquals(if (expected.cardId != CardId.NONE) expected.cardId.asString() else null, actual.cardId)
        Assertions.assertEquals(expected.dictionaryId.asString(), actual.dictionaryId)
        Assertions.assertEquals(expected.word, actual.word)
    }
}