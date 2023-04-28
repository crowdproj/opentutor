package com.gitlab.sszuev.flashcards.model.domain

import java.time.LocalDateTime

data class CardEntity(
    val cardId: CardId = CardId.NONE,
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val words: List<CardWordEntity> = emptyList(),
    val stats: Map<Stage, Long> = emptyMap(),
    val details: Map<String, Any> = emptyMap(),
    val answered: Int? = null,
    val changedAt: LocalDateTime = LocalDateTime.MIN,
) {
    companion object {
        val EMPTY = CardEntity()
    }
}

data class CardWordEntity(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<CardWordExampleEntity> = emptyList(),
    val translations: List<List<String>> = emptyList(),
    val sound: TTSResourceId = TTSResourceId.NONE,
)

data class CardWordExampleEntity(
    val text: String,
    val translation: String? = null,
)