package com.gitlab.sszuev.flashcards.utils

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DocumentEntity
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class JsonSupportTest {

    @Test
    fun `test DocumentEntity serialization & deserialization #1`() {
        val entity1 = CardEntity.EMPTY.copy(
            details = mapOf("b" to 42, "a" to "A", "c" to 424242.424242),
        )
        val entity2 = CardEntity.EMPTY.copy(
            stats = mapOf(Stage.SELF_TEST to 42),
        )
        val document1 = DocumentEntity(
            name = "XXX",
            sourceLang = LangEntity(LangId("S")),
            targetLang = LangEntity(LangId("T")),
            cards = listOf(entity1, entity2)
        )
        val res = document1.toJsonString()
        println(res)
        val document2 = documentEntityFromJson(res)
        assertEquals(document1, document2)
    }
}