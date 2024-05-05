package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.NONE
import kotlinx.datetime.Instant
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class CardEntity(
    val cardId: CardId = CardId.NONE,
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val words: List<CardWordEntity> = emptyList(),
    val stats: Map<Stage, Long> = emptyMap(),
    @Serializable
    val details: Map<String, @Polymorphic Any> = emptyMap(),
    val answered: Int? = null,
    val changedAt: Instant = Instant.NONE,
    val sound: TTSResourceId = TTSResourceId.NONE,
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
)

@Serializable
data class CardWordExampleEntity(
    val text: String,
    val translation: String? = null,
)