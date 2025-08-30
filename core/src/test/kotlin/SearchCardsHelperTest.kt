package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.core.processes.isSimilar
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class SearchCardsHelperTest {

    @Test
    fun `test isSimilar #1`() {
        val card1 = CardEntity(words = listOf(CardWordEntity("w")))
        val card2 = CardEntity(words = listOf(CardWordEntity("w")))
        Assertions.assertTrue(card1.isSimilar(card2))
    }

    @Test
    fun `test isSimilar #2`() {
        val card1 = CardEntity(words = listOf(CardWordEntity("w")))
        val card2 = CardEntity(words = listOf(CardWordEntity("q")))
        Assertions.assertFalse(card1.isSimilar(card2))
    }

    @Test
    fun `test isSimilar #3`() {
        val card1 = CardEntity(words = listOf(CardWordEntity("word")))
        val card2 = CardEntity(words = listOf(CardWordEntity("world")))
        Assertions.assertTrue(card1.isSimilar(card2))
    }

    @Test
    fun `test isSimilar #4`() {
        val card1 = CardEntity(words = listOf(CardWordEntity("ххх", translations = listOf(listOf("a", "slovo")))))
        val card2 = CardEntity(words = listOf(CardWordEntity("word", translations = listOf(listOf("b", "slo")))))
        Assertions.assertTrue(card1.isSimilar(card2))
    }

    @Test
    fun `test isSimilar #5`() {
        val card1 = CardEntity(
            words = listOf(
                CardWordEntity("moist", translations = listOf(listOf("сырой"), listOf("влажный"), listOf("мокрый")))
            )
        )
        val card2 = CardEntity(
            words = listOf(
                CardWordEntity("wet", translations = listOf(listOf("влажный"), listOf("сырой")))
            )
        )
        Assertions.assertTrue(card1.isSimilar(card2))
    }
}