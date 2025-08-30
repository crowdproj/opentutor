package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.mappers.v1.testutils.assertDictionary
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ToDictionaryTransportTest {

    @Test
    fun `test toGetAllDictionariesResponse`() {
        val context = DictionaryContext(
            requestId = AppRequestId("request=42"),
            operation = DictionaryOperation.GET_ALL_DICTIONARIES,
            responseDictionaryEntityList = listOf(
                DictionaryEntity(
                    dictionaryId = DictionaryId("J"),
                    name = "xxx",
                    totalCardsCount = 42,
                    learnedCardsCount = 21,
                    sourceLang = LangEntity(LangId("XX"), listOf("a", "b")),
                    targetLang = LangEntity(LangId("XX")),
                ),
                DictionaryEntity(
                    dictionaryId = DictionaryId("M"),
                    name = "yyy",
                    totalCardsCount = 42000,
                    sourceLang = LangEntity(LangId("GG")),
                    targetLang = LangEntity(LangId("DD"), listOf("d", "f")),
                ),
            ),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = context.toGetAllDictionaryResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertEquals(2, res.dictionaries!!.size)
        assertDictionary(context.responseDictionaryEntityList[0], res.dictionaries!![0])
        assertDictionary(context.responseDictionaryEntityList[1], res.dictionaries!![1])
    }

    @Test
    fun `test toUploadDictionaryResponse`() {
        val context = DictionaryContext(
            requestId = AppRequestId("request=42"),
            operation = DictionaryOperation.UPLOAD_DICTIONARY,
            responseDictionaryEntity = DictionaryEntity(
                dictionaryId = DictionaryId("J"),
                name = "xxx",
                totalCardsCount = 42,
                learnedCardsCount = 21,
                sourceLang = LangEntity(LangId("XX"), listOf("a", "b")),
                targetLang = LangEntity(LangId("XX")),
            ),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = context.toUploadDictionaryResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertNotNull(res.dictionary)
        assertDictionary(context.responseDictionaryEntity, res.dictionary!!)
    }

    @Test
    fun `test toCreateDictionaryResponse`() {
        val context = DictionaryContext(
            requestId = AppRequestId("request=42"),
            operation = DictionaryOperation.CREATE_DICTIONARY,
            responseDictionaryEntity = DictionaryEntity(
                dictionaryId = DictionaryId("I"),
                name = "test-dictionary",
                totalCardsCount = 1,
                learnedCardsCount = 2,
                sourceLang = LangEntity(LangId("S"), listOf("a", "b")),
                targetLang = LangEntity(LangId("T"), listOf("c", "d")),
            ),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = context.toCreateDictionaryResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertNotNull(res.dictionary)
        assertDictionary(context.responseDictionaryEntity, res.dictionary!!)
    }

    @Test
    fun `test toUpdateDictionaryResponse`() {
        val context = DictionaryContext(
            requestId = AppRequestId("request=42"),
            operation = DictionaryOperation.UPDATE_DICTIONARY,
            responseDictionaryEntity = DictionaryEntity(
                dictionaryId = DictionaryId("I"),
                name = "test-dictionary",
                totalCardsCount = 1,
                learnedCardsCount = 2,
                sourceLang = LangEntity(LangId("S"), listOf("a", "b")),
                targetLang = LangEntity(LangId("T"), listOf("c", "d")),
            ),
            errors = mutableListOf(),
            status = AppStatus.OK
        )
        val res = context.toUpdateDictionaryResponse()

        Assertions.assertEquals(context.requestId.asString(), res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertNotNull(res.dictionary)
        assertDictionary(context.responseDictionaryEntity, res.dictionary!!)
    }
}