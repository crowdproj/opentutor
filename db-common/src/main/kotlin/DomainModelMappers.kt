package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
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
    if (entity.word.isBlank()) {
        errors.add("blank word specified")
    }
    if (!entity.translations.flatten().any { it.isNotBlank() }) {
        errors.add("no translations specified")
    }
    return errors
}

fun validateCardLearns(learns: Collection<CardLearn>) {
    val ids = learns.groupBy { it.cardId }.filter { it.value.size > 1 }.map { it.key }
    require(ids.isEmpty()) { "Duplicate card ids: $ids" }
}

fun CardEntity.toCommonWordDtoList(): List<CommonWordDto> {
    val word = CommonWordDto(
        word = word,
        transcription = transcription,
        translations = translations,
        partOfSpeech = partOfSpeech,
        examples = this.examples.map { CommonExampleDto(it) },
    )
    return listOf(word)
}

fun CardEntity.toCommonCardDetailsDto(): CommonCardDetailsDto = this.details.toCommonCardDtoDetails()

fun CommonCardDetailsDto.toCardEntityDetails(): Map<Stage, Long> =
    this.mapKeys { Stage.valueOf(it.key) }.mapValues { it.value.toString().toLong() }

fun Id.asLong(): Long = if (this.asString().matches("\\d+".toRegex())) {
    this.asString().toLong()
} else {
    throw IllegalArgumentException("Wrong id specified: $this")
}