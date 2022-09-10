package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.LangId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ToDictionaryTransportTest {

    companion object {
        private fun assertDictionary(expected: DictionaryEntity, actual: DictionaryResource) {
            Assertions.assertEquals(
                if (expected.dictionaryId == DictionaryId.NONE) null else expected.dictionaryId.asString(),
                actual.dictionaryId
            )
            Assertions.assertEquals(expected.name, actual.name)
            Assertions.assertEquals(
                if (expected.sourceLangId == LangId.NONE) null else expected.sourceLangId.asString(),
                actual.sourceLang
            )
            Assertions.assertEquals(
                if (expected.targetLangId == LangId.NONE) null else expected.targetLangId.asString(),
                actual.targetLang
            )
            Assertions.assertEquals(expected.partsOfSpeech, actual.partsOfSpeech)
            Assertions.assertEquals(expected.totalCardsCount, actual.total)
            Assertions.assertEquals(expected.learnedCardsCount, actual.learned)
        }
    }

    @Test
    fun `test toGetAllDictionariesResponse`() {
        val context = DictionaryContext(
            requestId = AppRequestId("request=42"),
            operation = DictionaryOperation.GET_ALL_DICTIONARIES,
            responseDictionaryEntityList = listOf(
                DictionaryEntity(
                    dictionaryId = DictionaryId("J"),
                    name = "xxx",
                    partsOfSpeech = listOf("verb", "adjective"),
                    totalCardsCount = 42,
                    learnedCardsCount = 21,
                    sourceLangId = LangId("XX"),
                    targetLangId = LangId("XX"),
                ),
                DictionaryEntity(
                    dictionaryId = DictionaryId("M"),
                    name = "yyy",
                    totalCardsCount = 42000,
                    sourceLangId = LangId("GG"),
                    targetLangId = LangId("DD"),
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
}