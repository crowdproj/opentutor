package com.gitlab.sszuev.flashcards.logmappers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.logs.models.CardFilterLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardLearnLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardWordExampleLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardWordLogResource
import com.gitlab.sszuev.flashcards.logs.models.DictionaryLogResource
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FromContextTest {

    companion object {
        fun assertCardEntity(expected: CardEntity, actual: CardLogResource) {
            Assertions.assertEquals(expected.cardId.asString(), actual.cardId)
            Assertions.assertEquals(expected.dictionaryId.asString(), actual.dictionaryId)
            Assertions.assertEquals(expected.details, actual.details)
            Assertions.assertEquals(expected.stats.mapKeys { it.key.name }.takeIf { it.isNotEmpty() }, actual.stats)
            Assertions.assertEquals(expected.words.size, actual.words?.size)
            expected.words.forEachIndexed { index, e ->
                val a = actual.words?.get(index)
                Assertions.assertNotNull(a)
                assertCardWord(e, a!!)
            }
        }

        private fun assertCardWord(expected: CardWordEntity, actual: CardWordLogResource) {
            Assertions.assertEquals(expected.sound.takeIf { it != TTSResourceId.NONE }?.asString(), actual.sound)
            Assertions.assertEquals(expected.word, actual.word)
            Assertions.assertEquals(expected.transcription, actual.transcription)
            Assertions.assertEquals(expected.translations, actual.translations)
            Assertions.assertEquals(expected.partOfSpeech, actual.partOfSpeech)
            Assertions.assertEquals(expected.examples.size, actual.examples?.size)
            expected.examples.forEachIndexed { index, e ->
                val a = actual.examples?.get(index)
                Assertions.assertNotNull(a)
                assertCardExample(e, a!!)
            }
        }

        private fun assertCardExample(expected: CardWordExampleEntity, actual: CardWordExampleLogResource) {
            Assertions.assertEquals(expected.text, actual.text)
            Assertions.assertEquals(expected.translation, actual.translation)
        }

        fun assertCardFilter(expected: CardFilter, actual: CardFilterLogResource) {
            Assertions.assertEquals(expected.dictionaryIds.map { it.asString() }, actual.dictionaryIds)
            Assertions.assertEquals(expected.length, actual.length)
            Assertions.assertEquals(expected.random, actual.random)
            Assertions.assertEquals(expected.onlyUnknown, actual.unknown)
        }

        fun assertCardLearn(expected: CardLearn, actual: CardLearnLogResource) {
            Assertions.assertEquals(expected.cardId.asString(), actual.cardId)
            Assertions.assertEquals(expected.details.mapKeys { it.key.name }, actual.details)
        }

        fun assertDictionaryEntity(expected: DictionaryEntity, actual: DictionaryLogResource) {
            Assertions.assertEquals(expected.dictionaryId.asString(), actual.dictionaryId)
            Assertions.assertEquals(expected.name, actual.name)
        }
    }

    @Test
    fun `test get logs from card-context`() {
        val context = CardContext()
        context.requestId = AppRequestId("test-request-id")
        context.requestCardEntityId = CardId("test-request-card-id")
        context.requestDictionaryId = DictionaryId("test-request-dictionary-id")
        context.requestCardEntity = CardEntity(
            cardId = CardId("request-card-id"),
            dictionaryId = DictionaryId("request-dictionary-id"),
            words = listOf(
                CardWordEntity(
                    word = "XXX",
                    translations = listOf(listOf("a", "b"))
                )
            ),
            stats = mapOf(Stage.SELF_TEST to 42),
            details = mapOf("a" to 42L, "b" to listOf(42), "c" to kotlinx.datetime.Clock.System.now()),
        )
        context.responseCardEntity = CardEntity(
            cardId = CardId("response-card-id"),
            dictionaryId = DictionaryId("response-dictionary-id"),
            words = listOf(
                CardWordEntity(
                    word = "xxx",
                    translations = listOf(listOf("c", "d"))
                )
            )
        )
        context.responseCardEntityList = listOf(
            CardEntity(cardId = CardId("A")), CardEntity(cardId = CardId("B")),
        )
        context.requestCardLearnList = listOf(
            CardLearn(cardId = CardId("A"), details = mapOf(Stage.SELF_TEST to 10)),
            CardLearn(cardId = CardId("B"), details = mapOf(Stage.OPTIONS to 20))
        )
        context.requestCardFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("A"), DictionaryId("B")),
            length = 42,
            random = true,
            onlyUnknown = false,
        )

        val actual = context.toLogResource("test-log")

        Assertions.assertFalse(actual.logId.isNullOrBlank())
        Assertions.assertNotNull(actual.cards)
        Assertions.assertNull(actual.dictionaries)
        Assertions.assertEquals(context.requestId.asString(), actual.requestId)
        Assertions.assertEquals(context.requestCardEntityId.asString(), actual.cards!!.requestCardId)
        Assertions.assertEquals(context.requestDictionaryId.asString(), actual.cards!!.requestDictionaryId)
        Assertions.assertNotNull(actual.cards!!.requestCard)
        assertCardEntity(context.requestCardEntity, actual.cards!!.requestCard!!)
        Assertions.assertNotNull(actual.cards!!.responseCard)
        assertCardEntity(context.responseCardEntity, actual.cards!!.responseCard!!)
        Assertions.assertEquals(context.responseCardEntityList.size, actual.cards!!.responseCards!!.size)
        context.responseCardEntityList.forEachIndexed { i, e ->
            assertCardEntity(e, actual.cards!!.responseCards!![i])
        }
        Assertions.assertEquals(context.requestCardLearnList.size, actual.cards!!.requestCardLearn!!.size)
        context.requestCardLearnList.forEachIndexed { i, e ->
            assertCardLearn(e, actual.cards!!.requestCardLearn!![i])
        }
        assertCardFilter(context.requestCardFilter, actual.cards!!.requestCardFilter!!)
    }

    @Test
    fun `test get logs from dictionary-context`() {
        val context = DictionaryContext()
        context.requestId = AppRequestId("test-request-id")
        context.responseDictionaryEntityList = listOf(
            DictionaryEntity(dictionaryId = DictionaryId("A"), name = "A"),
            DictionaryEntity(dictionaryId = DictionaryId("B"), name = "B"),
        )

        val actual = context.toLogResource("test-log")

        Assertions.assertFalse(actual.logId.isNullOrBlank())
        Assertions.assertNull(actual.cards)
        Assertions.assertEquals(context.requestId.asString(), actual.requestId)
        Assertions.assertEquals(
            context.responseDictionaryEntityList.size,
            actual.dictionaries!!.responseDictionaries!!.size
        )
        context.responseDictionaryEntityList.forEachIndexed { i, e ->
            assertDictionaryEntity(e, actual.dictionaries!!.responseDictionaries!![i])
        }
    }
}