package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.SettingsEntity
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.repositories.DbUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class SettingsCorProcessorRunTest {

    companion object {
        private val testUserId = stubDictionary.userId

        @Suppress("SameParameterValue")
        private fun testContext(
            op: SettingsOperation,
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

        val context = testContext(SettingsOperation.GET_SETTINGS, userRepository)

        SettingsCorProcessor().execute(context)

        Assertions.assertEquals(
            SettingsEntity(
                numberOfWordsPerStage = 12,
                stageShowNumberOfWords = 13,
                stageOptionsNumberOfVariants = 14,
            ),
            context.responseSettingsEntity
        )
    }

    @Test
    fun `test update settings`() = runTest {
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

        val context = testContext(SettingsOperation.UPDATE_SETTINGS, userRepository)
        context.requestSettingsEntity = SettingsEntity(
            numberOfWordsPerStage = 14,
            stageShowNumberOfWords = 12,
            stageOptionsNumberOfVariants = 13,
        )

        SettingsCorProcessor().execute(context)

        Assertions.assertEquals(
            mapOf(
                "numberOfWordsPerStage" to 14,
                "stageShowNumberOfWords" to 12,
                "stageOptionsNumberOfVariants" to 13,
            ),
            updatedUser?.details
        )
    }
}