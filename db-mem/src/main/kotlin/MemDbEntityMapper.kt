package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.CommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonDictionaryDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonExampleDto
import com.gitlab.sszuev.flashcards.common.CommonUserDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonWordDto
import com.gitlab.sszuev.flashcards.common.LanguageRepository
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.CardStatus
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.parseCardDetailsJson
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.parseDictionaryDetailsJson
import com.gitlab.sszuev.flashcards.common.parseUserDetailsJson
import com.gitlab.sszuev.flashcards.common.toCommonWordDtoList
import com.gitlab.sszuev.flashcards.common.toDocumentExamples
import com.gitlab.sszuev.flashcards.common.toDocumentTranslations
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbExample
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbLanguage
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbUser
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbWord
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import java.util.UUID

internal fun MemDbUser.detailsAsJsonString(): String {
    return CommonUserDetailsDto(details).toJsonString()
}

internal fun fromJsonStringToMemDbUserDetails(json: String): Map<String, String> {
    return parseUserDetailsJson(json).mapValues { it.toString() }
}

internal fun MemDbDictionary.detailsAsJsonString(): String {
    return CommonDictionaryDetailsDto(this.details).toJsonString()
}

internal fun fromJsonStringToMemDbDictionaryDetails(json: String): Map<String, String> {
    return parseDictionaryDetailsJson(json).mapValues { it.toString() }
}

internal fun MemDbCard.detailsAsJsonString(): String {
    return CommonCardDetailsDto(this.details).toJsonString()
}

internal fun fromJsonStringToMemDbCardDetails(json: String): Map<String, String> {
    return parseCardDetailsJson(json).mapValues { it.toString() }
}

internal fun MemDbCard.wordsAsJsonString(): String {
    return words.map { it.toCommonWordDto() }.toJsonString()
}

internal fun fromJsonStringToMemDbWords(json: String): List<MemDbWord> {
    return parseCardWordsJson(json).map { it.toMemDbWord() }
}

internal fun MemDbUser.toAppUserEntity() = AppUserEntity(
    id = id?.asUserId() ?: AppUserId.NONE,
    authId = uuid.asUserUid(),
)

internal fun DocumentDictionary.toMemDbCards(mapAnswered: (CardStatus) -> Int): List<MemDbCard> {
    return this.cards.map { it.toMemDbCard(mapAnswered) }
}

internal fun DocumentDictionary.toMemDbDictionary(): MemDbDictionary {
    return MemDbDictionary(
        name = this.name,
        sourceLanguage = createMemDbLanguage(this.sourceLang),
        targetLanguage = createMemDbLanguage(this.targetLang),
        details = emptyMap(),
    )
}

internal fun fromDatabaseToDocumentDictionary(
    dictionary: MemDbDictionary,
    cards: List<MemDbCard>,
    mapStatus: (Int?) -> CardStatus
): DocumentDictionary {
    return DocumentDictionary(
        name = dictionary.name,
        sourceLang = dictionary.sourceLanguage.id,
        targetLang = dictionary.targetLanguage.id,
        cards = cards.map { it.toDocumentCard(mapStatus) },
    )
}

internal fun DocumentCard.toMemDbCard(mapAnswered: (CardStatus) -> Int): MemDbCard {
    return MemDbCard(
        text = this.text,
        words = this.toMemDbWords(),
        details = emptyMap(),
        answered = mapAnswered(this.status),
    )
}

private fun DocumentCard.toMemDbWords(): List<MemDbWord> {
    return toCommonWordDtoList().map { it.toMemDbWord() }
}

internal fun MemDbCard.toDocumentCard(mapStatus: (Int?) -> CardStatus): DocumentCard {
    val word = this.words.firstOrNull()?.toCommonWordDto()
    return DocumentCard(
        text = this.text,
        transcription = word?.transcription,
        partOfSpeech = word?.partOfSpeech,
        translations = word?.toDocumentTranslations() ?: emptyList(),
        examples = word?.toDocumentExamples() ?: emptyList(),
        status = mapStatus(this.answered),
    )
}

private fun MemDbExample.toEntityExample(): String {
    return if (translation != null) "$example -- $translation" else example
}

internal fun MemDbDictionary.toDictionaryEntity() = DictionaryEntity(
    dictionaryId = this.id?.asDictionaryId() ?: DictionaryId.NONE,
    name = this.name,
    sourceLang = this.sourceLanguage.toLangEntity(),
    targetLang = this.targetLanguage.toLangEntity(),
)

internal fun DictionaryEntity.toMemDbDictionary() = MemDbDictionary(
    id = if (this.dictionaryId == DictionaryId.NONE) null else this.dictionaryId.asLong(),
    name = this.name,
    sourceLanguage = this.sourceLang.toMemDbLanguage(),
    targetLanguage = this.targetLang.toMemDbLanguage(),
    details = emptyMap(),
)

internal fun MemDbCard.toCardEntity(): CardEntity {
    val word = this.words.firstOrNull()
    val details = this.details.mapKeys { Stage.valueOf(it.key) }.mapValues { it.value.toLong() }
    return CardEntity(
        cardId = id?.asCardId() ?: CardId.NONE,
        dictionaryId = dictionaryId?.asDictionaryId() ?: DictionaryId.NONE,
        word = text,
        transcription = word?.transcription,
        translations = word?.translations ?: emptyList(),
        examples = word?.examples?.map { it.toEntityExample() } ?: emptyList(),
        partOfSpeech = word?.partOfSpeech,
        details = details,
        answered = answered,
    )
}

internal fun CardEntity.toMemDbCard(): MemDbCard {
    val dictionaryId = dictionaryId.asLong()
    val cardExample = examples.map {
        MemDbExample(example = it, translation = null)
    }
    val cardWord = MemDbWord(
        word = word,
        transcription = transcription,
        translations = translations,
        partOfSpeech = partOfSpeech,
        examples = cardExample,
    )
    return MemDbCard(
        id = if (this.cardId == CardId.NONE) null else this.cardId.asLong(),
        dictionaryId = dictionaryId,
        text = word,
        words = listOf(cardWord),
        details = cardEntityDetailsToMemDbCardDetails(this.details),
        answered = answered,
        changedAt = null,
    )
}

internal fun cardEntityDetailsToMemDbCardDetails(details: Map<Stage, Long>) =
    details.map { it.key.name to it.value.toString() }.toMap()

internal fun MemDbLanguage.toLangEntity() = LangEntity(
    langId = this.id.asLangId(),
    partsOfSpeech = this.partsOfSpeech,
)

internal fun createMemDbLanguage(tag: String) = MemDbLanguage(
    id = tag,
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)

internal fun LangEntity.toMemDbLanguage() = MemDbLanguage(
    id = this.langId.asString(),
    partsOfSpeech = this.partsOfSpeech,
)

private fun MemDbWord.toCommonWordDto() = CommonWordDto(
    word = word,
    transcription = transcription,
    translations = translations,
    partOfSpeech = partOfSpeech,
    examples = examples.map { it.toCommonExampleDto() }
)

private fun MemDbExample.toCommonExampleDto() = CommonExampleDto(
    example = example,
    translation = translation,
)

private fun CommonWordDto.toMemDbWord() = MemDbWord(
    word = word,
    transcription = transcription,
    translations = translations,
    partOfSpeech = partOfSpeech,
    examples = examples.map { it.toMemDbExample() }
)

private fun CommonExampleDto.toMemDbExample() = MemDbExample(
    example = example,
    translation = translation,
)

private fun Long.asUserId() = AppUserId(toString())

private fun UUID.asUserUid() = AppAuthId(toString())

private fun String.asLangId(): LangId = LangId(this)

private fun Long.asCardId() = CardId(toString())

private fun Long.asDictionaryId() = DictionaryId(toString())