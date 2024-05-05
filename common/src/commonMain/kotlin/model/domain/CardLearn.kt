package com.gitlab.sszuev.flashcards.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class CardLearn (
    val cardId: CardId = CardId.NONE,
    val details: Map<Stage, Long> = emptyMap()
)