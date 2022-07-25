package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

internal fun Card.toEntity() = CardEntity(
    cardId = id.asCardId(),
    dictionaryId = dictionaryId.asDictionaryId(),
    word = text,
    transcription = transcription,
    translations = translations.map { it.text }.toList(),
    examples = examples.map { it.text }.toList(),
    partOfSpeech = partOfSpeech,
    details = details,
    answered = answered,
)

private fun Long.asDictionaryId() = DictionaryId(toString())

private fun Long.asCardId() = CardId(toString())