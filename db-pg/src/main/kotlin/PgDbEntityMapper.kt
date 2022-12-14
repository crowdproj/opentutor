package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.documents.DocumentLang
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionary
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

internal fun Dictionary.toDownloadResource(sysConfig: SysConfig, cards: SizedIterable<Card>) = DocumentDictionary(
    id = null,
    userId = null,
    name = this.name,
    sourceLang = this.sourceLang.toDocument(),
    targetLang = this.targetLand.toDocument(),
    cards = cards.map { it.toDownloadResource(sysConfig) }
)

internal fun Card.toDownloadResource(sysConfig: SysConfig) = DocumentCard(
    id = null,
    text = this.text,
    transcription = this.transcription,
    details = this.details ?: "",
    partOfSpeech = this.partOfSpeech,
    answered = this.answered,
    examples = this.examples.map { it.text },
    translations = this.translations.map { it.text },
    status = sysConfig.status(this.answered),
)

internal fun User.toEntity() = AppUserEntity(
    id = this.id.asUserId(),
    authId = this.uuid.asUserUid(),
)

internal fun Dictionary.toEntity() = DictionaryEntity(
    dictionaryId = this.id.asDictionaryId(),
    name = this.name,
    sourceLang = this.sourceLang.toEntity(),
    targetLang = this.targetLand.toEntity(),
)

internal fun Card.toEntity() = CardEntity(
    cardId = this.id.asCardId(),
    dictionaryId = this.dictionaryId.asDictionaryId(),
    word = this.text,
    transcription = this.transcription,
    details = toEntityDetails(this.details),
    partOfSpeech = this.partOfSpeech,
    answered = this.answered,
    examples = this.examples.map { it.text },
    translations = this.translations.map { toEntityTranslations(it.text) },
)

internal fun Language.toEntity() = LangEntity(
    langId = this.id.asLangId(),
    partsOfSpeech = this.partsOfSpeech.split(",")
)

private fun Language.toDocument() = DocumentLang(
    tag = this.id.value,
    partsOfSpeech = this.partsOfSpeech.split(",")
)

internal fun copyToDbEntityRecord(from: CardEntity, to: Card) {
    to.dictionaryId = from.dictionaryId.asRecordId()
    to.text = from.word
    to.transcription = from.transcription
    to.partOfSpeech = from.partOfSpeech
    to.answered = from.answered
    to.details = toDbRecordDetails(from.details)
}

internal fun copyToDbEntityRecord(dictionaryId: EntityID<Long>, from: DocumentCard, to: Card) {
    to.dictionaryId = dictionaryId
    to.text = from.text
    to.transcription = from.transcription
    to.partOfSpeech = from.partOfSpeech
    to.answered = from.answered
    to.details = from.details
}

internal fun copyToDbExampleRecord(txt: String, card: Card, to: Example) {
    to.cardId = card.id
    to.text = txt
}

internal fun copyToDbTranslationRecord(txt: List<String>, card: Card, to: Translation) {
    to.cardId = card.id
    to.text = toDbRecordTranslations(txt)
}

internal fun EntityID<String>.asLangId() = LangId(value)

internal fun EntityID<Long>.asUserId() = AppUserId(value.toString())

internal fun UUID.asUserUid() = AppAuthId(toString())

internal fun EntityID<Long>.asDictionaryId() = DictionaryId(value.toString())

internal fun EntityID<Long>.asCardId() = CardId(value.toString())

internal fun AppUserId.asRecordId(): EntityID<Long> = EntityID(asLong(), Users)

internal fun DictionaryId.asRecordId() = EntityID(asLong(), Dictionaries)

internal fun CardId.asRecordId() = EntityID(asLong(), Cards)

internal fun String.asRecordId() = EntityID(this, Languages)

internal fun partsOfSpeechToRecordTxt(partsOfSpeech: Collection<String>): String {
    return partsOfSpeech.joinToString(",")
}