package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation
import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.gitlab.sszuev.flashcards.translation.impl.MockTranslationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TranslationCorProcessorRunTest {
    companion object {

        private fun testContext(repository: TranslationRepository): TranslationContext {
            val context = TranslationContext(
                operation = TranslationOperation.FETCH_CARD,
                repository = repository
            )
            context.requestAppAuthId = AppAuthId("42")
            context.requestId = requestId()
            return context
        }

        private fun requestId(): AppRequestId {
            return AppRequestId("[for-${TranslationOperation.FETCH_CARD}]")
        }
    }

    @Test
    fun `test get resource success`() = runTest {
        val testSrcLang = LangId("EN")
        val testDstLang = LangId(" Ru ")
        val testQuery = "qqq"
        val testTCard = listOf(
            TranslationEntity(
                word = "q",
                translations = listOf(listOf("a", "b", "c")),
            )
        )

        val testCardEntity = CardEntity(
            words = listOf(
                CardWordEntity(
                    word = "q",
                    translations = listOf(listOf("a", "b", "c")),
                    sound = TTSResourceId("en:q"),
                    primary = true,
                )
            )
        )

        var fetchCardCount = 0
        val repository = MockTranslationRepository(
            invokeFetch = { _, _, _ ->
                fetchCardCount++
                testTCard
            },
        )

        val context = testContext(repository)
        context.requestSourceLang = testSrcLang
        context.requestTargetLang = testDstLang
        context.requestWord = testQuery

        TranslationCorProcessor().execute(context)

        Assertions.assertEquals(1, fetchCardCount)
        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testCardEntity, context.responseCardEntity)
    }
}