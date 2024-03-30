package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.repositories.DbCard

fun CardEntity.toDbCard() = DbCard(
    cardId = this.cardId.asString(),
    dictionaryId = this.dictionaryId.asString(),
    answered = this.answered,
    changedAt = this.changedAt,
    stats = this.stats.mapKeys { it.key.name },
    words = this.words.map { it.toDbCardWord() },
    details = this.details,
)

fun DbCard.toCardEntity() = CardEntity(
    cardId = CardId(this.cardId),
    dictionaryId = DictionaryId(this.dictionaryId),
    answered = this.answered,
    changedAt = this.changedAt,
    stats = this.stats.mapKeys { Stage.valueOf(it.key) },
    words = this.words.map { it.toCardWordEntity() },
    details = this.details,
)

private fun CardWordEntity.toDbCardWord() = DbCard.Word(
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toDbCardWordExample() },
    translations = this.translations,
)

private fun CardWordExampleEntity.toDbCardWordExample() =
    DbCard.Word.Example(text = this.text, translation = this.translation)

private fun DbCard.Word.toCardWordEntity() = CardWordEntity(
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toCardWordExampleEntity() },
    translations = this.translations,
)

private fun DbCard.Word.Example.toCardWordExampleEntity() =
    CardWordExampleEntity(text = this.text, translation = this.translation)