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

class ToTransportTest {

    @Test
    fun `test toGetCardResponse`() {
        val context = AppContext(
            requestId = RequestId("request=42"),
            operation = Operation.GET_CARD,
            responseEntity = CardEntity(
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
        assertCard(context.responseEntity, res.card!!)
    }

    private fun assertError(expected: Error, actual: ErrorResource) {
        Assertions.assertEquals(expected.code, actual.code)
        Assertions.assertEquals(expected.message, actual.message)
        Assertions.assertEquals(expected.field, actual.field)
        Assertions.assertEquals(expected.group, actual.group)
    }

    private fun assertCard(expected: CardEntity, actual: CardResource) {
        Assertions.assertEquals(expected.cardId.asString(), actual.cardId)
        Assertions.assertEquals(expected.dictionaryId.asString(), actual.dictionaryId)
        Assertions.assertEquals(expected.word, actual.word)
    }
}