package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.asJava
import com.gitlab.sszuev.flashcards.asKotlin
import com.gitlab.sszuev.flashcards.common.CommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonDictionaryDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonExampleDto
import com.gitlab.sszuev.flashcards.common.CommonUserDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonWordDto
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.toCardEntityDetails
import com.gitlab.sszuev.flashcards.common.toCardEntityStats
import com.gitlab.sszuev.flashcards.common.toCardWordEntity
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.common.wordsAsCommonWordDtoList
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbExample
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbLanguage
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbUser
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbWord
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbLang
import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.LanguageRepository

internal fun MemDbDictionary.detailsAsJsonString(): String {
    return CommonDictionaryDetailsDto(this.details.toMutableMap()).toJsonString()
}

internal fun MemDbCard.detailsAsJsonString(): String {
    return detailsAsCommonCardDetailsDto().toJsonString()
}

internal fun MemDbUser.detailsAsJsonString(): String {
    return detailsAsCommonUserDetailsDto().toJsonString()
}

internal fun MemDbCard.wordsAsJsonString(): String {
    return words.map { it.toCommonWordDto() }.toJsonString()
}

internal fun fromJsonStringToMemDbWords(json: String): List<MemDbWord> {
    return parseCardWordsJson(json).map { it.toMemDbWord() }
}

internal fun MemDbDictionary.toDbDictionary() = DbDictionary(
    dictionaryId = this.id?.toString() ?: "",
    userId = this.userId ?: "",
    name = this.name,
    sourceLang = this.sourceLanguage.toDbLang(),
    targetLang = this.targetLanguage.toDbLang(),
    details = this.details,
)

internal fun DbDictionary.toMemDbDictionary(): MemDbDictionary = MemDbDictionary(
    id = if (this.dictionaryId.isBlank()) null else this.dictionaryId.toLong(),
    name = this.name,
    sourceLanguage = this.sourceLang.toMemDbLanguage(),
    targetLanguage = this.targetLang.toMemDbLanguage(),
    details = this.details,
    userId = this.userId.ifBlank { null }
)

internal fun MemDbCard.toDbCard(): DbCard {
    val details: CommonCardDetailsDto = this.detailsAsCommonCardDetailsDto()
    return DbCard(
        cardId = id?.toString() ?: "",
        dictionaryId = dictionaryId?.toString() ?: "",
        words = this.words.map { it.toCommonWordDto() }.map { it.toCardWordEntity() },
        details = details.toCardEntityDetails(),
        stats = details.toCardEntityStats(),
        answered = this.answered,
        changedAt = this.changedAt.asKotlin(),
    )
}

internal fun DbCard.toMemDbCard(): MemDbCard {
    val dictionaryId = dictionaryId.toLong()
    return MemDbCard(
        id = if (this.cardId.isBlank()) null else this.cardId.toLong(),
        dictionaryId = dictionaryId,
        words = this.wordsAsCommonWordDtoList().map { it.toMemDbWord() },
        details = this.detailsAsCommonCardDetailsDto().toMemDbCardDetails(),
        answered = this.answered,
        changedAt = this.changedAt.asJava(),
    )
}

internal fun createMemDbLanguage(tag: String): MemDbLanguage = MemDbLanguage(
    id = tag,
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)

internal fun DbLang.toMemDbLanguage(): MemDbLanguage = MemDbLanguage(
    id = this.langId,
    partsOfSpeech = this.partsOfSpeech,
)

internal fun MemDbLanguage.toDbLang(): DbLang = DbLang(
    langId = this.id,
    partsOfSpeech = this.partsOfSpeech,
)

internal fun DbUser.toMemDbUser(): MemDbUser = MemDbUser(
    id = this.id,
    createdAt = this.createdAt.asJava(),
    details = this.details,
)

internal fun MemDbUser.toDbUser(): DbUser = DbUser(
    id = this.id,
    createdAt = this.createdAt.asKotlin(),
    details = this.details,
)

private fun MemDbWord.toCommonWordDto(): CommonWordDto = CommonWordDto(
    word = word,
    transcription = transcription,
    translations = translations,
    partOfSpeech = partOfSpeech,
    examples = examples.map { it.toCommonExampleDto() },
    primary = primary,
)

private fun MemDbExample.toCommonExampleDto(): CommonExampleDto = CommonExampleDto(
    text = text,
    translation = translation,
)

private fun CommonWordDto.toMemDbWord(): MemDbWord = MemDbWord(
    word = word,
    transcription = transcription,
    translations = translations,
    partOfSpeech = partOfSpeech,
    examples = examples.map { it.toMemDbExample() },
    primary = primary,
)

private fun CommonExampleDto.toMemDbExample(): MemDbExample = MemDbExample(
    text = text,
    translation = translation,
)

internal fun MemDbCard.detailsAsCommonCardDetailsDto(): CommonCardDetailsDto =
    CommonCardDetailsDto(this.details.toMutableMap())

internal fun MemDbUser.detailsAsCommonUserDetailsDto(): CommonUserDetailsDto =
    CommonUserDetailsDto(this.details.toMutableMap())

private fun CommonCardDetailsDto.toMemDbCardDetails(): Map<String, String> = this.mapValues { it.value.toString() }
