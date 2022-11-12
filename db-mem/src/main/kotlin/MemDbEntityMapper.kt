package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.common.documents.CardStatus
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.documents.DocumentLang
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbLanguage
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbUser
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.*
import java.util.*

internal fun MemDbUser.toEntity() = AppUserEntity(
    id = id.asUserId(),
    authId = uuid.asUserUid(),
)

internal fun DocumentDictionary.toDbRecord(userId: AppUserId? = null): MemDbDictionary {
    val id = requireNotNull(this.id) { "no dictionary id found" }
    return MemDbDictionary(
        userId = userId?.asLong() ?: this.userId,
        id = id,
        name = this.name,
        sourceLanguage = this.sourceLang.toDbRecord(),
        targetLanguage = this.targetLang.toDbRecord(),
        cards = this.cards.asSequence().map { it.toDbRecord(id) }.associateBy { it.id }.toMutableMap(),
    )
}

internal fun MemDbDictionary.toDocument(withIds: Boolean = true, mapStatus: (Int?) -> CardStatus): DocumentDictionary {
    return DocumentDictionary(
        userId = if (withIds) this.userId else null,
        id = if (withIds) this.id else null,
        name = this.name,
        sourceLang = this.sourceLanguage.toDocument(),
        targetLang = this.targetLanguage.toDocument(),
        cards = this.cards.values.map { it.toDocument(withIds, mapStatus) },
    )
}

private fun DocumentCard.toDbRecord(dictionaryId: Long) = MemDbCard(
    id = requireNotNull(this.id) { "no card id found" },
    dictionaryId = dictionaryId,
    text = this.text,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    details = this.details,
    answered = this.answered,
    translations = this.translations,
    examples = this.examples,
)

private fun MemDbCard.toDocument(withIds: Boolean = true, mapStatus: (Int?) -> CardStatus) = DocumentCard(
    id = if (withIds) this.id else null,
    text = this.text,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    details = this.details,
    answered = this.answered,
    translations = this.translations,
    examples = this.examples,
    status = mapStatus(this.answered),
)

internal fun MemDbDictionary.toEntity() = DictionaryEntity(
    dictionaryId = this.id.asDictionaryId(),
    name = this.name,
    sourceLang = this.sourceLanguage.toEntity(),
    targetLang = this.targetLanguage.toEntity(),
)

internal fun DictionaryEntity.toDbRecord(userId: AppUserId) = MemDbDictionary(
    userId = userId.asLong(),
    id = this.dictionaryId.asLong(),
    name = this.name,
    sourceLanguage = this.sourceLang.toDbRecord(),
    targetLanguage = this.targetLang.toDbRecord(),
    cards = mutableMapOf(),
)

internal fun MemDbCard.toEntity() = CardEntity(
    cardId = id.asCardId(),
    dictionaryId = dictionaryId.asDictionaryId(),
    word = text,
    transcription = transcription,
    translations = translations.map { toEntityTranslations(it) },
    examples = examples,
    partOfSpeech = partOfSpeech,
    details = toEntityDetails(details),
    answered = answered,
)

internal fun CardEntity.toDbRecord(cardId: Long? = null): MemDbCard {
    val dictionaryId = dictionaryId.asLong()
    return MemDbCard(
        id = cardId ?: this.cardId.asLong(),
        dictionaryId = dictionaryId,
        text = word,
        transcription = transcription,
        translations = translations.map { toDbRecordTranslations(it) },
        examples = examples,
        partOfSpeech = partOfSpeech,
        details = toDbRecordDetails(details),
        answered = answered,
    )
}

internal fun MemDbLanguage.toEntity() = LangEntity(
    langId = this.id.asLangId(),
    partsOfSpeech = this.partsOfSpeech,
)

internal fun LangEntity.toDbRecord() = MemDbLanguage(
    id = this.langId.asString(),
    partsOfSpeech = this.partsOfSpeech,
)

private fun DocumentLang.toDbRecord() = MemDbLanguage(
    id = this.tag,
    partsOfSpeech = this.partsOfSpeech,
)

private fun MemDbLanguage.toDocument() = DocumentLang(
    tag = this.id,
    partsOfSpeech = this.partsOfSpeech,
)

private fun Long.asUserId() = AppUserId(toString())

private fun UUID.asUserUid() = AppAuthId(toString())

private fun String.asLangId(): LangId = LangId(this)

private fun Long.asCardId() = CardId(toString())

private fun Long.asDictionaryId() = DictionaryId(toString())