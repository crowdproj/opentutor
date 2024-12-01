package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.translation.api.TCard
import com.gitlab.sszuev.flashcards.translation.api.TWord

internal fun TCard.toCardEntity() = CardEntity(
    words = this.words.map { it.toCardWord() },
)

internal fun TWord.toCardWord() = CardWordEntity(
    word = this.word,
    partOfSpeech = this.partOfSpeech,
    translations = this.translations,
    examples = this.examples.map { CardWordExampleEntity(it.text, it.translation) }
)