package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.model.common.Error
import com.gitlab.sszuev.flashcards.model.common.Operation
import com.gitlab.sszuev.flashcards.model.common.RequestId
import com.gitlab.sszuev.flashcards.model.common.Status
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ToTransportTest {

    @Test
    fun `test toGetCardResponse`() {
        val context = AppContext(
            requestId = RequestId("request=42"),
            operation = Operation.GET_CARD,
            responseCardEntity = CardEntity(
                cardId = CardId("card=42"),
                dictionaryId = DictionaryId("dictionary=42"),
                word = "xxx"
            ),
            errors = mutableListOf(
                Error(
                    code = "42",
                    message = "XXX",
                    field = "YYY",
                    group = "GGG",
                )
            ),
            status = Status.FAIL
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
        val context = AppContext(
            requestId = RequestId("request=42"),
            operation = Operation.GET_CARDS,
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
                Error(
                    code = "a",
                    message = "b",
                    field = "c",
                    group = "d",
                ),
                Error(
                    code = "e",
                    message = "f",
                    field = "g",
                    group = "h",
                )
            ),
            status = Status.OK
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
        value = Operation::class,
        names = ["CREATE_CARD", "UPDATE_CARD", "DELETE_CARD", "LEARN_CARD", "RESET_CARD"]
    )
    fun `test toCreateUpdateCardResponse`(op: Operation) {
        val context = AppContext(
            requestId = RequestId(op.name),
            operation = op,
            responseCardEntity =
            CardEntity(
                cardId = CardId("A"),
                dictionaryId = DictionaryId("G"),
                word = "xxx"
            ),
            errors = mutableListOf(),
            status = Status.OK
        )
        val res = when (op) {
            Operation.UPDATE_CARD -> context.toUpdateCardResponse()
            Operation.CREATE_CARD -> context.toCreateCardResponse()
            Operation.DELETE_CARD -> context.toDeleteCardResponse()
            Operation.LEARN_CARD -> context.toLearnCardResponse()
            Operation.RESET_CARD-> context.toResetCardResponse()
            else -> throw IllegalArgumentException()
        }

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
    }

    private fun assertError(expected: Error, actual: ErrorResource) {
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