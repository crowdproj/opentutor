package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.toEntityDetails
import com.gitlab.sszuev.flashcards.common.toEntityTranslations
import com.gitlab.sszuev.flashcards.dbpg.dao.Card
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
    translations = toEntityTranslations(this.translations.map { it.text }),
)

private fun EntityID<Long>.asDictionaryId() = DictionaryId(value.toString())

private fun EntityID<Long>.asCardId() = CardId(value.toString())