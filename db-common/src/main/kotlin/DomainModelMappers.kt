package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage

fun validateCardEntityForCreate(entity: CardEntity) {
    val errors = mutableListOf<String>()
    if (entity.cardId != CardId.NONE) {
        errors.add("no card-id specified")
    }
    errors.addAll(validateCardEntity(entity))
    require(errors.isEmpty()) {
        "Card $entity does not pass the validation. Errors = $errors"
    }
}

fun validateCardEntityForUpdate(entity: CardEntity) {
    val errors = mutableListOf<String>()
    if (entity.cardId == CardId.NONE) {
        errors.add("no card-id specified.")
    }
    errors.addAll(validateCardEntity(entity))
    require(errors.isEmpty()) {
        "Card $entity does not pass the validation. Errors = $errors"
    }
}

private fun validateCardEntity(entity: CardEntity): List<String> {
    val errors = mutableListOf<String>()
    if (entity.dictionaryId == DictionaryId.NONE) {
        errors.add("no dictionary-id specified")
    }
    if (entity.words.isEmpty()) {
        errors.add("no words specified")
    }
    errors.addAll(validateCardWords(entity.words))
    return errors
}

private fun validateCardWords(words: List<CardWordEntity>): List<String> {
    val errors = mutableListOf<String>()
    val translations = words.flatMap { it.translations.flatten() }.filter { it.isNotBlank() }
    if (translations.isEmpty()) {
        errors.add("${words.map { it.word }} :: no translations specified")
    }
    return errors
}

fun validateCardLearns(learns: Collection<CardLearn>) {
    val ids = learns.groupBy { it.cardId }.filter { it.value.size > 1 }.map { it.key }
    require(ids.isEmpty()) { "Duplicate card ids: $ids" }
}

fun CardEntity.wordsAsCommonWordDtoList(): List<CommonWordDto> = words.map { it.toCommonWordDto() }

fun CardWordEntity.toCommonWordDto(): CommonWordDto = CommonWordDto(
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toCommonExampleDto() },
    translations = this.translations,
)

fun CommonWordDto.toCardWordEntity(): CardWordEntity = CardWordEntity(
    word = word,
    transcription = transcription,
    translations = translations,
    partOfSpeech = partOfSpeech,
    examples = examples.map { it.toCardWordExampleEntity() }
)

private fun CommonExampleDto.toCardWordExampleEntity(): CardWordExampleEntity = CardWordExampleEntity(
    text = text,
    translation = translation,
)

fun CardEntity.detailsAsCommonCardDetailsDto(): CommonCardDetailsDto {
    return CommonCardDetailsDto(this.details + this.stats.mapKeys { it.key.name })
}

fun CardWordExampleEntity.toCommonExampleDto(): CommonExampleDto = CommonExampleDto(
    text = this.text,
    translation = this.translation,
)

fun CommonCardDetailsDto.toCardEntityStats(): Map<Stage, Long> =
    this.filterKeys { Stage.entries.map { s -> s.name }.contains(it) }
        .mapKeys { Stage.valueOf(it.key) }
        .mapValues { it.value.toString().toLong() }

fun CommonCardDetailsDto.toCardEntityDetails(): Map<String, Any> =
    this.filterKeys { !Stage.entries.map { s -> s.name }.contains(it) }

fun Id.asLong(): Long = if (this.asString().matches("\\d+".toRegex())) {
    this.asString().toLong()
} else {
    throw IllegalArgumentException("Wrong id specified: $this")
}