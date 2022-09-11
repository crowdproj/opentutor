package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.DebugResource
import com.gitlab.sszuev.flashcards.api.v1.models.DebugStub
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.RunMode
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppStub
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FromDictionaryTransportTest {

    companion object {

        @Suppress("SameParameterValue")
        private fun assertContext(
            expectedStub: AppStub,
            expectedMode: AppMode,
            expectedRequestId: String,
            actual: DictionaryContext
        ) {
            Assertions.assertEquals(expectedStub, actual.debugCase)
            Assertions.assertEquals(expectedMode, actual.workMode)
            Assertions.assertEquals(expectedRequestId, actual.requestId.asString())
        }
    }

    @Test
    fun `test fromGetAllDictionariesRequest`() {
        val req = GetAllDictionariesRequest(
            requestId = "request=42",
            debug = DebugResource(
                mode = RunMode.STUB,
                stub = DebugStub.SUCCESS
            ),
        )
        val context = DictionaryContext()
        context.fromDictionaryTransport(req)

        assertContext(
            expectedStub = AppStub.SUCCESS,
            expectedMode = AppMode.STUB,
            expectedRequestId = "request=42",
            actual = context
        )
    }
}