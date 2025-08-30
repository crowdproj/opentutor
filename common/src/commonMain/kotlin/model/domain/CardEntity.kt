package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.utils.MapStringAnySerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class CardEntity(
    val cardId: CardId = CardId.NONE,
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val words: List<CardWordEntity> = emptyList(),
    val stats: Map<Stage, Long> = emptyMap(),
    @Serializable(with = MapStringAnySerializer::class)
    val details: Map<String, @Polymorphic Any> = emptyMap(),
    val answered: Int? = null,
    val changedAt: Instant = Instant.NONE,
) {
    companion object {
        val EMPTY = CardEntity()
    }
}

@Serializable
data class CardWordEntity(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<CardWordExampleEntity> = emptyList(),
    val translations: List<List<String>> = emptyList(),
    val sound: TTSResourceId = TTSResourceId.NONE,
    val primary: Boolean = false,
)

@Serializable
data class CardWordExampleEntity(
    val text: String,
    val translation: String? = null,
)