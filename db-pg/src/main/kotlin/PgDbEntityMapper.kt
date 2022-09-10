package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.toDbRecordDetails
import com.gitlab.sszuev.flashcards.common.toDbRecordTranslations
import com.gitlab.sszuev.flashcards.common.toEntityDetails
import com.gitlab.sszuev.flashcards.common.toEntityTranslations
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionary
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.*
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

internal fun User.toEntity() = AppUserEntity(
    id = this.id.asUserId(),
    authId = this.uuid.asUserUid(),
)

internal fun Dictionary.toEntity() = DictionaryEntity(
    dictionaryId = this.id.asDictionaryId(),
    name = this.name,
    sourceLangId = this.sourceLang.asLangId(),
    targetLangId = this.targetLand.asLangId(),
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

internal fun copyToDbEntityRecord(from: CardEntity, to: Card) {
    to.dictionaryId = from.dictionaryId.asRecordId()
    to.text = from.word
    to.transcription = from.transcription
    to.partOfSpeech = from.partOfSpeech
    to.answered = from.answered
    to.details = toDbRecordDetails(from.details)
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

internal fun AppUserId.asRecordId(): EntityID<Long> {
    return if (asString().matches("\\d+".toRegex())) {
        EntityID(asString().toLong(), Dictionaries)
    } else {
        EntityID(-42, Dictionaries)
    }
}

internal fun DictionaryId.asRecordId() = EntityID(asString().toLong(), Dictionaries)

internal fun CardId.asRecordId() = EntityID(asString().toLong(), Cards)