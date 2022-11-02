package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
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

    @Test
    fun `test fromUploadDictionariesRequest`() {
        val req = UploadDictionaryRequest(
            requestId = "request=42",
            debug = DebugResource(
                mode = RunMode.STUB,
                stub = DebugStub.SUCCESS
            ),
            resource = byteArrayOf(42),
        )
        val context = DictionaryContext()
        context.fromDictionaryTransport(req)

        Assertions.assertEquals(context.requestDictionaryResourceEntity, ResourceEntity(DictionaryId.NONE, byteArrayOf(42)))
        assertContext(
            expectedStub = AppStub.SUCCESS,
            expectedMode = AppMode.STUB,
            expectedRequestId = "request=42",
            actual = context
        )
    }
}