package com.gitlab.sszuev.flashcards.utils

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class CborSupportTest {

    @Test
    fun `test CardContext serialization & deserialization #1`() {
        val entity1 = CardEntity.EMPTY.copy(
            details = mapOf("b" to 42, "c" to 42.42, "a" to "A", "d" to 4242.4242),
        )
        val entity2 = CardEntity.EMPTY.copy(
            stats = mapOf(Stage.SELF_TEST to 42),
        )
        val context1 = CardContext(
            requestCardEntity = entity1,
            responseCardEntityList = listOf(entity1, entity2),
            errors = mutableListOf(
                AppError(group = "X", message = "x"),
            ),
        )
        val res = context1.toByteArray()
        val context2 = cardContextFromByteArray(res)
        assertEquals(context1, context2)
    }

    @Test
    fun `test CardContext serialization & deserialization #2`() {
        val context1 = CardContext(
            errors = mutableListOf(
                AppError(group = "X", message = "x", exception = IllegalArgumentException("ex")),
            ),
        )
        val res = context1.toByteArray()
        val context2 = cardContextFromByteArray(res)
        assertEquals(context1.copy(errors = mutableListOf()), context2.copy(errors = mutableListOf()))
        assertEquals(context1.errors.single().copy(exception = null), context2.errors.single().copy(exception = null))
        assertTrue(context2.errors.single().exception is IllegalArgumentException)
        assertEquals("ex", context2.errors.single().exception!!.message)
    }

    @Test
    fun `test DictionaryContext serialization & deserialization`() {
        val context1 = DictionaryContext(
            requestDictionaryResourceEntity = ResourceEntity(
                resourceId = DictionaryId("qq"),
                data = ByteArray(42) { 42 },
            ),
            errors = mutableListOf(
                AppError(group = "Q", message = "q"),
            ),
        )
        val res = context1.toByteArray()
        val context2 = dictionaryContextFromByteArray(res)
        assertEquals(context1, context2)
    }

    @Test
    fun `test TTSCContext serialization & deserialization`() {
        val context1 = TTSContext(
            normalizedRequestTTSResourceGet = TTSResourceGet(
                lang = LangId("xx"),
                word = "xxx",
            ),
            responseTTSResourceEntity = ResourceEntity(
                resourceId = TTSResourceId(""),
                data = ByteArray(42) { 42 },
            ),
            errors = mutableListOf(
                AppError(group = "X", message = "x"),
            ),
        )
        val res = context1.toByteArray()
        val context2 = ttsContextFromByteArray(res)
        assertEquals(context1, context2)
    }
}