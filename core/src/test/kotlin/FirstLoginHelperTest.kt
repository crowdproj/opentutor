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
                Arguments.of("xx", "weather", "天气"), // unknown locale => en->zh
                Arguments.of("zh", "weather", "天气"),
                Arguments.of("en", "天气", "weather"),
                Arguments.of("es", "weather", "clima"),
                Arguments.of("pt", "weather", "tempo"),
                Arguments.of("ja", "weather", "天気"),
                Arguments.of("de", "weather", "Wetter"),
                Arguments.of("fr", "weather", "météo"),
                Arguments.of("it", "weather", "tempo"),
                Arguments.of("pl", "weather", "pogoda"),
                Arguments.of("nl", "weather", "weer"),
                Arguments.of("el", "weather", "καιρός"),
                Arguments.of("hu", "weather", "időjárás"),
                Arguments.of("cs", "weather", "počasí"),
                Arguments.of("sv", "weather", "väder"),
                Arguments.of("bg", "weather", "времето"),
                Arguments.of("da", "weather", "vejr"),
                Arguments.of("fi", "weather", "sää"),
                Arguments.of("sk", "weather", "počasie"),
                Arguments.of("lt", "weather", "oras"),
                Arguments.of("lv", "weather", "laikapstākļi"),
                Arguments.of("sl", "weather", "vreme"),
                Arguments.of("et", "weather", "ilm"),
                Arguments.of("mt", "weather", "temp"),
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
        userRepository.createOrUpdateUser(id = id, language = "xx") {
            counts.incrementAndGet()
        }
        userRepository.createOrUpdateUser(id = id, language = "xx") {
            counts.incrementAndGet()
        }
        Assertions.assertEquals(1, counts.get())
        Assertions.assertEquals(1, data.size)

        users.invalidateAll()
        userRepository.createOrUpdateUser(id = id, language = "xx") {
            counts.incrementAndGet()
        }
        Assertions.assertEquals(1, counts.get())
        Assertions.assertEquals(1, data.size)
    }

    @ParameterizedTest
    @MethodSource(value = ["builtIntVocabularies"])
    fun `test load builtin documents`(locale: String, word: String, expected: String) {
        val document = loadBuiltinDocuments(locale).single()
        val actual =
            document.cards.single { it.words.single().word == word }.words.single().translations.single().single()
        Assertions.assertEquals(expected, actual)
    }
}