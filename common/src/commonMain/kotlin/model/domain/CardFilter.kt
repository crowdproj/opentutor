package com.gitlab.sszuev.flashcards.model.domain

data class CardFilter(
    val dictionaryIds: List<DictionaryId> = emptyList(),
    val random: Boolean = false,
    val length: Int = 0,
    val withUnknown: Boolean = false,
) {
    companion object {
        val EMPTY = CardFilter()
    }
}
