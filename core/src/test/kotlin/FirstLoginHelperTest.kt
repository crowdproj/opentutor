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
        private fun builtInDictionaries(): List<Arguments> {
            return listOf(
                Arguments.of(
                    "zh", listOf(
                        mapOf("be" to "是", "write" to "写"),
                        mapOf("weather" to "天气"),
                    )
                ),
                Arguments.of(
                    "uk", listOf(
                        mapOf("be" to "бути", "write" to "писати"),
                    )
                ),
                Arguments.of(
                    "es",
                    listOf(
                        mapOf("be" to "ser", "write" to "escribir"),
                        mapOf("weather" to "clima"),
                    )
                ),
                Arguments.of(
                    "pt",
                    listOf(
                        mapOf("be" to "ser", "write" to "escrever"),
                        mapOf("weather" to "tempo"),
                    )
                ),
                Arguments.of(
                    "ja",
                    listOf(
                        mapOf("be" to "なれ", "write" to "書く"),
                        mapOf("weather" to "天気"),
                    )
                ),
                Arguments.of(
                    "de",
                    listOf(
                        mapOf("be" to "Sei", "write" to "schreiben"),
                        mapOf("weather" to "Wetter"),
                    )
                ),
                Arguments.of(
                    "fr",
                    listOf(
                        mapOf("be" to "être", "write" to "écrire"),
                        mapOf("weather" to "météo"),
                    )
                ),
                Arguments.of(
                    "it",
                    listOf(
                        mapOf("be" to "Essere", "write" to "scrivere"),
                        mapOf("weather" to "tempo"),
                    )
                ),
                Arguments.of(
                    "pl",
                    listOf(
                        mapOf("be" to "Być", "write" to "pisać"),
                        mapOf("weather" to "pogoda")
                    ),
                ),
                Arguments.of(
                    "nl",
                    listOf(
                        mapOf("read" to "lezen", "write" to "schrijven"),
                        mapOf("weather" to "weer"),
                    )
                ),
                Arguments.of(
                    "el",
                    listOf(
                        mapOf("read" to "διαβάζω", "write" to "γράφω"),
                        mapOf("weather" to "καιρός"),
                    )
                ),
                Arguments.of(
                    "hu",
                    listOf(
                        mapOf("read" to "olvas", "write" to "írj"),
                        mapOf("weather" to "időjárás"),
                    )
                ),
                Arguments.of(
                    "cs",
                    listOf(
                        mapOf("read" to "číst", "write" to "psát"),
                        mapOf("weather" to "počasí"),
                    )
                ),
                Arguments.of(
                    "sv",
                    listOf(
                        mapOf("read" to "läsa", "write" to "skriva"),
                        mapOf("weather" to "väder"),
                    )
                ),
                Arguments.of(
                    "bg",
                    listOf(
                        mapOf("read" to "прочети", "write" to "пиша"),
                        mapOf("weather" to "времето"),
                    )
                ),
                Arguments.of(
                    "da",
                    listOf(
                        mapOf("read" to "læse", "write" to "skrive"),
                        mapOf("weather" to "vejr"),
                    )
                ),
                Arguments.of(
                    "fi",
                    listOf(
                        mapOf("read" to "lukea", "write" to "kirjoittaa"),
                        mapOf("weather" to "sää"),
                    )
                ),
                Arguments.of(
                    "sk",
                    listOf(
                        mapOf("read" to "čítať", "write" to "písať"),
                        mapOf("weather" to "počasie"),
                    )
                ),
                Arguments.of(
                    "lt",
                    listOf(
                        mapOf("read" to "skaityti", "write" to "rašyti"),
                        mapOf("weather" to "oras"),
                    )
                ),
                Arguments.of(
                    "lv",
                    listOf(
                        mapOf("read" to "lasīt", "write" to "rakstīt"),
                        mapOf("weather" to "laikapstākļi"),
                    )
                ),
                Arguments.of(
                    "sl",
                    listOf(
                        mapOf("read" to "brati", "write" to "pisati"),
                        mapOf("weather" to "vreme"),
                    )
                ),
                Arguments.of(
                    "et",
                    listOf(
                        mapOf("read" to "lugema", "write" to "kirjutama"),
                        mapOf("weather" to "ilm"),
                    )
                ),
                Arguments.of(
                    "mt",
                    listOf(
                        mapOf("read" to "taqra", "write" to "tikteb"),
                        mapOf("weather" to "temp"),
                    )
                ),
                Arguments.of(
                    "hi",
                    listOf(
                        mapOf("read" to "पढ़ना", "write" to "लिखना"),
                        mapOf("weather" to "मौसम"),
                    )
                ),
                Arguments.of(
                    "ar",
                    listOf(
                        mapOf("read" to "يقرأ", "write" to "يكتب"),
                        mapOf("weather" to "طقس"),
                    )
                ),
                Arguments.of(
                    "bn",
                    listOf(
                        mapOf("read" to "পড়া", "write" to "লেখা"),
                        mapOf("weather" to "আবহাওয়া"),
                    )
                ),
                Arguments.of(
                    "pa",
                    listOf(
                        mapOf("read" to "ਪੜ੍ਹਨਾ", "write" to "ਲਿਖਣਾ"),
                        mapOf("weather" to "ਮੌਸਮ"),
                    )
                ),
                Arguments.of(
                    "vi",
                    listOf(
                        mapOf("read" to "đọc", "write" to "viết"),
                        mapOf("weather" to "thời tiết"),
                    )
                ),
                Arguments.of(
                    "mr",
                    listOf(
                        mapOf("read" to "वाचणे", "write" to "लिहिणे"),
                        mapOf("weather" to "हवामान"),
                    )
                ),
                Arguments.of(
                    "te",
                    listOf(
                        mapOf("read" to "చదవు", "write" to "రాయు"),
                        mapOf("weather" to "వాతావరణం"),
                    )
                ),
                Arguments.of(
                    "jv",
                    listOf(
                        mapOf("read" to "maca", "write" to "nulis"),
                        mapOf("weather" to "cuaca"),
                    )
                ),
                Arguments.of(
                    "ko",
                    listOf(
                        mapOf("read" to "읽다", "write" to "쓰다"),
                        mapOf("weather" to "날씨"),
                    )
                ),
                Arguments.of(
                    "ta",
                    listOf(
                        mapOf("read" to "படி", "write" to "எழுது"),
                        mapOf("weather" to "வானிலை"),
                    )
                ),
                Arguments.of(
                    "tr",
                    listOf(
                        mapOf("read" to "okumak", "write" to "yazmak"),
                        mapOf("weather" to "hava durumu"),
                    )
                ),
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

    @Test
    fun `test load builtin documents for default locale (ru)`() {
        val documents = loadBuiltinDocuments("xx").toList()
        Assertions.assertEquals(
            setOf(
                "Irregular Verbs",
                "Weather",
                "Common Words #01",
                "Common Words #02",
                "Common Words #03",
                "Common Words #04",
                "Common Words #05",
                "Common Words #06",
                "Common Words #07",
                "Common Words #08",
                "Common Words #08",
                "Common Words #10",
                "Common Words #11",
                "Blindsight, Peter Watts",
            ),
            documents.map { it.name }.toSet()
        )
    }

    @Test
    fun `test load builtin documents for en`() {
        val documents = loadBuiltinDocuments("en").toList()
        Assertions.assertEquals(
            setOf(
                "Weather RU-EN",
                "Weather FR-EN",
                "Weather DE-EN",
                "Weather ES-EN",
                "Weather IT-EN",
                "Weather PT-EN",
                "Weather ZH-EN",
                "Weather JA-EN",
                "Weather KO-EN",
                "Weather TR-EN",
                "Weather PL-EN",
                "Weather UK-EN",
                "Weather AR-EN",
            ),
            documents.map { it.name }.toSet()
        )
    }

    @ParameterizedTest
    @MethodSource(value = ["builtInDictionaries"])
    fun `test load builtin documents`(locale: String, given: List<Map<String, String>>) {
        val documents = loadBuiltinDocuments(locale).toList()
        Assertions.assertEquals(documents.size, given.size)
        given.forEachIndexed { index, map ->
            val document = documents[index]
            map.forEach { (word, expected) ->
                val actual = document.cards
                    .single { it.words.first().word == word }
                    .words.first()
                    .translations.single().single()
                Assertions.assertEquals(expected, actual)
            }
        }
    }
}