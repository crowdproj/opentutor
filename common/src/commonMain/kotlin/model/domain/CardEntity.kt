package com.gitlab.sszuev.flashcards.model.domain

data class CardEntity(
    val cardId: CardId = CardId.NONE,
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val word: String = "",
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val details: Map<Stage, Long> = emptyMap(),
    val answered: Int? = null,
    val translations: List<List<String>> = emptyList(),
    val examples: List<String> = emptyList(),
    val sound: ResourceId = ResourceId.NONE,
) {
    companion object {
        val EMPTY = CardEntity()
    }
}