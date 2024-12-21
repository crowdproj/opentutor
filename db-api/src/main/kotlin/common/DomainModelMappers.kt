package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.repositories.DbCadStage
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbUser

fun validateCardEntityForCreate(entity: DbCard) {
    val errors = mutableListOf<String>()
    if (entity.cardId.isNotBlank()) {
        errors.add("card-id specified")
    }
    errors.addAll(validateCardEntity(entity))
    require(errors.isEmpty()) {
        "Card $entity does not pass the validation. Errors = $errors"
    }
}

fun validateCardEntityForUpdate(entity: DbCard) {
    val errors = mutableListOf<String>()
    if (entity.cardId.isBlank()) {
        errors.add("no card-id specified.")
    }
    errors.addAll(validateCardEntity(entity))
    require(errors.isEmpty()) {
        "Card $entity does not pass the validation. Errors = $errors"
    }
}

private fun validateCardEntity(entity: DbCard): List<String> {
    val errors = mutableListOf<String>()
    if (entity.dictionaryId.isBlank()) {
        errors.add("no dictionary-id specified")
    }
    if (entity.words.isEmpty()) {
        errors.add("no words specified")
    }
    errors.addAll(validateCardWords(entity.words))
    return errors
}

private fun validateCardWords(words: List<DbCard.Word>): List<String> {
    val errors = mutableListOf<String>()
    val translations = words.flatMap { it.translations.flatten() }.filter { it.isNotBlank() }
    if (translations.isEmpty()) {
        errors.add("${words.map { it.word }} :: no translations specified")
    }
    return errors
}

fun DbCard.wordsAsCommonWordDtoList(): List<CommonWordDto> = words.map { it.toCommonWordDto() }

fun DbCard.Word.toCommonWordDto(): CommonWordDto = CommonWordDto(
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toCommonExampleDto() },
    translations = this.translations,
    primary = this.primary,
)

fun CommonWordDto.toCardWordEntity(): DbCard.Word = DbCard.Word(
    word = this.word,
    transcription = this.transcription,
    translations = this.translations,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toCardWordExampleEntity() },
    primary = this.primary,
)

private fun CommonExampleDto.toCardWordExampleEntity(): DbCard.Word.Example = DbCard.Word.Example(
    text = text,
    translation = translation,
)

fun DbDictionary.detailsAsCommonCardDetailsDto(): CommonCardDetailsDto {
    return CommonCardDetailsDto(this.details.toMutableMap())
}

fun DbCard.detailsAsCommonCardDetailsDto(): CommonCardDetailsDto {
    return CommonCardDetailsDto((this.details + this.stats.mapKeys { it.key }).toMutableMap())
}

fun DbUser.detailsAsCommonUserDetailsDto(): CommonUserDetailsDto {
    return CommonUserDetailsDto(this.details.toMutableMap())
}

fun DbCard.Word.Example.toCommonExampleDto(): CommonExampleDto = CommonExampleDto(
    text = this.text,
    translation = this.translation,
)

fun CommonCardDetailsDto.toCardEntityStats(): Map<String, Long> =
    this.filterKeys { DbCadStage.entries.map { s -> s.name }.contains(it) }
        .mapValues { it.value.toString().toLong() }

fun CommonCardDetailsDto.toCardEntityDetails(): Map<String, Any> =
    this.filterKeys { !DbCadStage.entries.map { s -> s.name }.contains(it) }
