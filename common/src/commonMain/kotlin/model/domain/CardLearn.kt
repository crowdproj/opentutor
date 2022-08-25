package com.gitlab.sszuev.flashcards.model.domain

data class CardLearn (
    val cardId: CardId = CardId.NONE,
    val details: Map<Stage, Long> = emptyMap()
)