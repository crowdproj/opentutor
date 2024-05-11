package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

internal class TTSCorProcessorValidationTest {

    companion object {
        private const val PARAMETERIZED_TEST_NAME =
            "test: ${ParameterizedTest.INDEX_PLACEHOLDER}: \"${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}\""
        private val requestId = UUID.randomUUID().toString()
        private val processor = TTSCorProcessor()

        @JvmStatic
        private fun wrongLangIds(): List<String> {
            return listOf("", "xxxxxx", "xxx:", "en~", "42")
        }

        @JvmStatic
        private fun wrongWords(): List<String> {
            return listOf("", "x".repeat(42_000))
        }

        @Suppress("SameParameterValue")
        private fun testContext(op: TTSOperation): TTSContext {
            val context = TTSContext(operation = op)
            context.requestAppAuthId = AppAuthId("42")
            context.requestId = AppRequestId(requestId)
            return context
        }

        private fun error(context: TTSContext): AppError {
            val errors = context.errors
            Assertions.assertEquals(1, errors.size) {
                "Got errors: ${errors.map { it.field }}"
            }
            return errors[0]
        }

        private fun assertValidationError(expectedField: String, actual: AppError) {
            Assertions.assertEquals("validators", actual.group)
            Assertions.assertEquals(expectedField, actual.field)
            Assertions.assertEquals("validation-$expectedField", actual.code)
        }
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource(value = ["wrongLangIds"])
    fun `test get resource - validate request LangId`(id: String) = runTest {
        val context = testContext(TTSOperation.GET_RESOURCE)
        context.requestTTSResourceGet = TTSResourceGet(lang = LangId(id), word = "xxx")
        processor.execute(context)
        val error = error(context)
        assertValidationError("audio-resource-lang-id", error)
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource(value = ["wrongWords"])
    fun `test get resource - validate request word`(word: String) = runTest {
        val context = testContext(TTSOperation.GET_RESOURCE)
        context.requestTTSResourceGet = TTSResourceGet(lang = LangId("EN"), word = word)
        processor.execute(context)
        val error = error(context)
        assertValidationError("audio-resource-word", error)
    }
}