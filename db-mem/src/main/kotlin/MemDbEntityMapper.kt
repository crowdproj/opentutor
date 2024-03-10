package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.CommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonDictionaryDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonExampleDto
import com.gitlab.sszuev.flashcards.common.CommonUserDetailsDto
import com.gitlab.sszuev.flashcards.common.CommonWordDto
import com.gitlab.sszuev.flashcards.common.LanguageRepository
import com.gitlab.sszuev.flashcards.common.asJava
import com.gitlab.sszuev.flashcards.common.asKotlin
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.parseCardDetailsJson
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.parseDictionaryDetailsJson
import com.gitlab.sszuev.flashcards.common.parseUserDetailsJson
import com.gitlab.sszuev.flashcards.common.toCardEntityDetails
import com.gitlab.sszuev.flashcards.common.toCardEntityStats
import com.gitlab.sszuev.flashcards.common.toCardWordEntity
import com.gitlab.sszuev.flashcards.common.toCommonWordDtoList
import com.gitlab.sszuev.flashcards.common.toDocumentExamples
import com.gitlab.sszuev.flashcards.common.toDocumentTranslations
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.common.wordsAsCommonWordDtoList
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
    return detailsAsCommonCardDetailsDto().toJsonString()
}

internal fun fromJsonStringToMemDbCardDetails(json: String): Map<String, String> {
    return parseCardDetailsJson(json).mapValues { it.value.toString() }
}

internal fun MemDbCard.wordsAsJsonString(): String {
    return words.map { it.toCommonWordDto() }.toJsonString()
}

internal fun fromJsonStringToMemDbWords(json: String): List<MemDbWord> {
    return parseCardWordsJson(json).map { it.toMemDbWord() }
}

internal fun MemDbUser.toAppUserEntity(): AppUserEntity = AppUserEntity(
    id = id?.asUserId() ?: AppUserId.NONE,
    authId = uuid.asAppAuthId(),
)

internal fun DocumentDictionary.toMemDbCards(mapAnswered: (DocumentCardStatus) -> Int): List<MemDbCard> {
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
    mapStatus: (Int?) -> DocumentCardStatus
): DocumentDictionary {
    return DocumentDictionary(
        name = dictionary.name,
        sourceLang = dictionary.sourceLanguage.id,
        targetLang = dictionary.targetLanguage.id,
        cards = cards.map { it.toDocumentCard(mapStatus) },
    )
}

internal fun DocumentCard.toMemDbCard(mapAnswered: (DocumentCardStatus) -> Int): MemDbCard {
    return MemDbCard(
        words = this.toMemDbWords(),
        details = emptyMap(),
        answered = mapAnswered(this.status),
    )
}

private fun DocumentCard.toMemDbWords(): List<MemDbWord> {
    return toCommonWordDtoList().map { it.toMemDbWord() }
}

internal fun MemDbCard.toDocumentCard(mapStatus: (Int?) -> DocumentCardStatus): DocumentCard {
    val word = this.words.first().toCommonWordDto()
    return DocumentCard(
        text = word.word,
        transcription = word.transcription,
        partOfSpeech = word.partOfSpeech,
        translations = word.toDocumentTranslations(),
        examples = word.toDocumentExamples(),
        status = mapStatus(this.answered),
    )
}

internal fun MemDbDictionary.toDictionaryEntity(): DictionaryEntity = DictionaryEntity(
    dictionaryId = this.id?.asDictionaryId() ?: DictionaryId.NONE,
    name = this.name,
    sourceLang = this.sourceLanguage.toLangEntity(),
    targetLang = this.targetLanguage.toLangEntity(),
)

internal fun DictionaryEntity.toMemDbDictionary(): MemDbDictionary = MemDbDictionary(
    id = if (this.dictionaryId == DictionaryId.NONE) null else this.dictionaryId.asLong(),
    name = this.name,
    sourceLanguage = this.sourceLang.toMemDbLanguage(),
    targetLanguage = this.targetLang.toMemDbLanguage(),
    details = emptyMap(),
)

internal fun MemDbCard.toCardEntity(): CardEntity {
    val details: CommonCardDetailsDto = this.detailsAsCommonCardDetailsDto()
    return CardEntity(
        cardId = id?.asCardId() ?: CardId.NONE,
        dictionaryId = dictionaryId?.asDictionaryId() ?: DictionaryId.NONE,
        words = this.words.map { it.toCommonWordDto() }.map { it.toCardWordEntity() },
        details = details.toCardEntityDetails(),
        stats = details.toCardEntityStats(),
        answered = this.answered,
        changedAt = this.changedAt.asKotlin(),
    )
}

internal fun CardEntity.toMemDbCard(): MemDbCard {
    val dictionaryId = dictionaryId.asLong()
    return MemDbCard(
        id = if (this.cardId == CardId.NONE) null else this.cardId.asLong(),
        dictionaryId = dictionaryId,
        words = this.wordsAsCommonWordDtoList().map { it.toMemDbWord() },
        details = this.detailsAsCommonCardDetailsDto().toMemDbCardDetails(),
        answered = this.answered,
        changedAt = this.changedAt.asJava(),
    )
}

internal fun MemDbLanguage.toLangEntity(): LangEntity = LangEntity(
    langId = this.id.asLangId(),
    partsOfSpeech = this.partsOfSpeech,
)

internal fun createMemDbLanguage(tag: String): MemDbLanguage = MemDbLanguage(
    id = tag,
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)

internal fun LangEntity.toMemDbLanguage(): MemDbLanguage = MemDbLanguage(
    id = this.langId.asString(),
    partsOfSpeech = this.partsOfSpeech,
)

private fun MemDbWord.toCommonWordDto(): CommonWordDto = CommonWordDto(
    word = word,
    transcription = transcription,
    translations = translations,
    partOfSpeech = partOfSpeech,
    examples = examples.map { it.toCommonExampleDto() }
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
    examples = examples.map { it.toMemDbExample() }
)

private fun CommonExampleDto.toMemDbExample(): MemDbExample = MemDbExample(
    text = text,
    translation = translation,
)

internal fun MemDbCard.detailsAsCommonCardDetailsDto(): CommonCardDetailsDto = CommonCardDetailsDto(this.details)

private fun CommonCardDetailsDto.toMemDbCardDetails(): Map<String, String> = this.mapValues { it.value.toString() }

private fun Long.asUserId(): AppUserId = AppUserId(toString())

private fun String.asLangId(): LangId = LangId(this)

internal fun Long.asCardId(): CardId = CardId(toString())

internal fun Long.asDictionaryId(): DictionaryId = DictionaryId(toString())

internal fun Long?.asDictionaryId(): DictionaryId = DictionaryId(checkNotNull(this).toString())

private fun UUID.asAppAuthId(): AppAuthId = AppAuthId(toString())