package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.DebugResource
import com.gitlab.sszuev.flashcards.api.v1.models.DebugStub
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.RunMode
import com.gitlab.sszuev.flashcards.model.common.Mode
import com.gitlab.sszuev.flashcards.model.common.Stub
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
        val context = AppContext()
        context.fromTransport(req)

        Assertions.assertEquals(Stub.SUCCESS, context.debugCase)
        Assertions.assertEquals(Mode.STUB, context.workMode)
        Assertions.assertEquals("card=42", context.requestEntity.cardId.asString())
        Assertions.assertEquals("request=42", context.requestId.asString())
    }
}