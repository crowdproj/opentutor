package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.CreateDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UploadDictionaryRequest
import com.gitlab.sszuev.flashcards.mappers.v1.testutils.assertDictionary
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FromDictionaryTransportTest {

    companion object {

        @Suppress("SameParameterValue")
        private fun assertContext(
            expectedRequestId: String,
            actual: DictionaryContext
        ) {
            Assertions.assertEquals(expectedRequestId, actual.requestId.asString())
        }
    }

    @Test
    fun `test fromGetAllDictionariesRequest`() {
        val req = GetAllDictionariesRequest(
            requestId = "request=42",
        )
        val context = DictionaryContext()
        context.fromDictionaryTransport(req)

        assertContext(
            expectedRequestId = "request=42",
            actual = context
        )
    }

    @Test
    fun `test fromUploadDictionariesRequest`() {
        val req = UploadDictionaryRequest(
            requestId = "request=42",
            resource = byteArrayOf(42),
        )
        val context = DictionaryContext()
        context.fromDictionaryTransport(req)

        Assertions.assertEquals(
            context.requestDictionaryResourceEntity,
            ResourceEntity(DictionaryId.NONE, byteArrayOf(42))
        )
        assertContext(
            expectedRequestId = "request=42",
            actual = context
        )
    }

    @Test
    fun `test fromCreateDictionaryRequest`() {
        val req = CreateDictionaryRequest(
            requestId = "request=42",
            dictionary = DictionaryResource(
                name = "xxx",
                sourceLang = "1",
                targetLang = "2",
                partsOfSpeech = listOf("a", "b"),
                total = 3,
                learned = 4,
            )
        )
        val context = DictionaryContext()
        context.fromDictionaryTransport(req)

        assertContext(
            expectedRequestId = "request=42",
            actual = context
        )
        assertDictionary(req.dictionary!!, context.requestDictionaryEntity)
    }
}