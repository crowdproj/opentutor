package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.core.processes.createOrUpdateUser
import com.gitlab.sszuev.flashcards.core.processes.loadBuiltinDocuments
import com.gitlab.sszuev.flashcards.core.processes.users
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.repositories.DbUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.atomic.AtomicInteger

internal class FirstLoginHelperTest {

    companion object {

        @JvmStatic
        private fun builtIntVocabularies(): List<Arguments> {
            return listOf(
                Arguments.of("zh", "天气"),
                Arguments.of("es", "clima")
            )
        }
    }

    @Test
    fun `test create or update user`() {
        val id = AppAuthId("42")
        val data = mutableMapOf<String, DbUser>()
        val userRepository = MockDbUserRepository(
            invokeCreateUser = {
                data[it.id] = it
                it
            },
            invokeUpdateUser = {
                val new = data[it.id]!!
                data[it.id] = new
                new
            },
            invokeFindUserById = {
                data[it]
            }
        )

        val counts = AtomicInteger()
        userRepository.createOrUpdateUser(id = id, locale = "xx") {
            counts.incrementAndGet()
        }
        userRepository.createOrUpdateUser(id = id, locale = "xx") {
            counts.incrementAndGet()
        }
        Assertions.assertEquals(1, counts.get())
        Assertions.assertEquals(1, data.size)

        users.invalidateAll()
        userRepository.createOrUpdateUser(id = id, locale = "xx") {
            counts.incrementAndGet()
        }
        Assertions.assertEquals(1, counts.get())
        Assertions.assertEquals(1, data.size)
    }

    @ParameterizedTest
    @MethodSource(value = ["builtIntVocabularies"])
    fun `test load builtin documents`(locale: String, word: String) {
        val document = loadBuiltinDocuments(locale).single()
        val actual =
            document.cards.single { it.words.single().word == "weather" }.words.single().translations.single().single()
        Assertions.assertEquals(word, actual)
    }
}