package com.gitlab.sszuev.flashcards.dbpg

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.gitlab.sszuev.flashcards.common.LanguageRepository
import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.toDocumentTranslations
import com.gitlab.sszuev.flashcards.common.toEntityTranslations
import com.gitlab.sszuev.flashcards.dbpg.dao.Card
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionary
import com.gitlab.sszuev.flashcards.dbpg.dao.Example
import com.gitlab.sszuev.flashcards.dbpg.dao.Language
import com.gitlab.sszuev.flashcards.dbpg.dao.Translation
import com.gitlab.sszuev.flashcards.dbpg.dao.User
import com.gitlab.sszuev.flashcards.dbpg.dao.Users
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
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.UUID

private val mapper: ObjectMapper = ObjectMapper()
private val detailsTypeReference: TypeReference<Map<Stage, Long>> =
    object : TypeReference<Map<Stage, Long>>() {}

internal fun Dictionary.toDownloadResource(sysConfig: SysConfig, cards: SizedIterable<Card>) = DocumentDictionary(
    name = this.name,
    sourceLang = this.sourceLang.id.value,
    targetLang = this.targetLand.id.value,
    cards = cards.map { it.toDownloadResource(sysConfig) }
)

internal fun Card.toDownloadResource(sysConfig: SysConfig) = DocumentCard(
    text = this.text,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
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
    to.details = "{}"
}

internal fun copyToDbExampleRecord(txt: String, card: Card, to: Example) {
    to.cardId = card.id
    to.text = txt
}

internal fun copyToDbTranslationRecord(txt: List<String>, card: Card, to: Translation) {
    to.cardId = card.id
    to.text = toDocumentTranslations(txt)
}

fun toEntityDetails(fromDb: String?): Map<Stage, Long> {
    return if (fromDb.isNullOrBlank()) {
        emptyMap()
    } else try {
        mapper.readValue(fromDb, detailsTypeReference)
    } catch (e: JsonProcessingException) {
        emptyMap()
    }
}

fun toDbRecordDetails(details: Map<Stage, Long>): String {
    return try {
        mapper.writeValueAsString(details)
    } catch (e: JsonProcessingException) {
        throw IllegalStateException("Can't convert $details to string", e)
    }
}

internal fun EntityID<String>.asLangId() = LangId(value)

internal fun EntityID<Long>.asUserId() = AppUserId(value.toString())

internal fun UUID.asUserUid() = AppAuthId(toString())

internal fun EntityID<Long>.asDictionaryId() = DictionaryId(value.toString())

internal fun EntityID<Long>.asCardId() = CardId(value.toString())

internal fun AppUserId.asRecordId(): EntityID<Long> = EntityID(asLong(), Users)

internal fun DictionaryId.asRecordId() = EntityID(asLong(), Dictionaries)

internal fun CardId.asRecordId() = EntityID(asLong(), Cards)

internal fun createLangEntity(tag: String) = LangEntity(
    langId = LangId(tag),
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)