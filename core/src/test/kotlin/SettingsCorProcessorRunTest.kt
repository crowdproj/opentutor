package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.SettingsEntity
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.repositories.DbUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class SettingsCorProcessorRunTest {

    companion object {

        @Suppress("SameParameterValue")
        private fun testContext(
            op: SettingsOperation,
            testUserId: AppAuthId,
            userRepository: MockDbUserRepository = MockDbUserRepository(),
        ): SettingsContext {
            val context = SettingsContext(
                operation = op,
                repositories = DbRepositories().copy(
                    userRepository = userRepository,
                )
            )
            context.requestAppAuthId = testUserId
            context.requestId = requestId(op)
            return context
        }

        private fun requestId(op: SettingsOperation): AppRequestId {
            return AppRequestId("[for-${op}]")
        }
    }

    @Test
    fun `test get settings`() = runTest {
        val testUserId = AppAuthId("1")
        val userRepository = MockDbUserRepository(
            invokeFindUserById = {
                DbUser(
                    id = it,
                    details = mapOf(
                        "numberOfWordsPerStage" to 12,
                        "stageShowNumberOfWords" to 13,
                        "stageOptionsNumberOfVariants" to 14,
                    )
                )
            },
            invokeCreateUser = { Assertions.fail() },
            invokeUpdateUser = { Assertions.fail() },
        )

        val context = testContext(
            op = SettingsOperation.GET_SETTINGS,
            testUserId = testUserId,
            userRepository = userRepository,
        )

        SettingsCorProcessor().execute(context)

        Assertions.assertEquals(
            SettingsEntity(
                numberOfWordsPerStage = 12,
                stageShowNumberOfWords = 13,
                stageOptionsNumberOfVariants = 14,
                stageMosaicSourceLangToTargetLang = true,
                stageOptionsSourceLangToTargetLang = true,
                stageWritingSourceLangToTargetLang = true,
                stageSelfTestSourceLangToTargetLang = true,
                stageMosaicTargetLangToSourceLang = false,
                stageOptionsTargetLangToSourceLang = false,
                stageWritingTargetLangToSourceLang = false,
                stageSelfTestTargetLangToSourceLang = false,
            ),
            context.responseSettingsEntity
        )
    }

    @Test
    fun `test update settings`() = runTest {
        val testUserId = AppAuthId("2")
        var updatedUser: DbUser? = null
        val userRepository = MockDbUserRepository(
            invokeFindUserById = {
                DbUser(
                    id = it,
                    details = mapOf(
                        "numberOfWordsPerStage" to 12,
                        "stageShowNumberOfWords" to 13,
                        "stageOptionsNumberOfVariants" to 14,
                    )
                )
            },
            invokeCreateUser = { Assertions.fail() },
            invokeUpdateUser = {
                updatedUser = it
                it
            },
        )

        val context = testContext(
            op = SettingsOperation.UPDATE_SETTINGS,
            testUserId = testUserId,
            userRepository = userRepository,
        )
        context.requestSettingsEntity = SettingsEntity(
            numberOfWordsPerStage = 14,
            stageShowNumberOfWords = 12,
            stageOptionsNumberOfVariants = 13,
            stageMosaicSourceLangToTargetLang = false,
            stageOptionsSourceLangToTargetLang = true,
            stageWritingSourceLangToTargetLang = false,
            stageSelfTestSourceLangToTargetLang = true,
            stageMosaicTargetLangToSourceLang = false,
            stageOptionsTargetLangToSourceLang = true,
            stageWritingTargetLangToSourceLang = false,
            stageSelfTestTargetLangToSourceLang = true,
        )

        SettingsCorProcessor().execute(context)

        Assertions.assertEquals(
            mapOf(
                "numberOfWordsPerStage" to 14,
                "stageShowNumberOfWords" to 12,
                "stageOptionsNumberOfVariants" to 13,
                "stageMosaicSourceLangToTargetLang" to false,
                "stageOptionsSourceLangToTargetLang" to true,
                "stageWritingSourceLangToTargetLang" to false,
                "stageSelfTestSourceLangToTargetLang" to true,
                "stageMosaicTargetLangToSourceLang" to false,
                "stageOptionsTargetLangToSourceLang" to true,
                "stageWritingTargetLangToSourceLang" to false,
                "stageSelfTestTargetLangToSourceLang" to true,
            ),
            updatedUser?.details
        )
    }
}