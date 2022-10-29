package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.*
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
                if (expected.sourceLang == LangEntity.EMPTY) null else expected.sourceLang.langId.asString(),
                actual.sourceLang
            )
            Assertions.assertEquals(
                if (expected.targetLang == LangEntity.EMPTY) null else expected.targetLang.langId.asString(),
                actual.targetLang
            )
            Assertions.assertEquals(
                expected.sourceLang.takeIf { it != LangEntity.EMPTY }?.partsOfSpeech,
                actual.partsOfSpeech
            )
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
}