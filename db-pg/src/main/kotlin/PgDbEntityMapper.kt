package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.toDbRecordDetails
import com.gitlab.sszuev.flashcards.common.toDbRecordTranslations
import com.gitlab.sszuev.flashcards.common.toEntityDetails
import com.gitlab.sszuev.flashcards.common.toEntityTranslations
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import org.jetbrains.exposed.dao.id.EntityID

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

internal fun EntityID<Long>.asDictionaryId() = DictionaryId(value.toString())

internal fun EntityID<Long>.asCardId() = CardId(value.toString())

internal fun DictionaryId.asRecordId() = EntityID(asString().toLong(), Dictionaries)

internal fun CardId.asRecordId() = EntityID(asString().toLong(), Cards)