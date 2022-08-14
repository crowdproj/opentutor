package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.dbmem.dao.Example
import com.gitlab.sszuev.flashcards.dbmem.dao.IdSequences
import com.gitlab.sszuev.flashcards.dbmem.dao.Translation
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

internal fun Card.toEntity() = CardEntity(
    cardId = id.asCardId(),
    dictionaryId = dictionaryId.asDictionaryId(),
    word = text,
    transcription = transcription,
    translations = translations.map { toEntityTranslations(it.text) },
    examples = examples.map { it.text },
    partOfSpeech = partOfSpeech,
    details = toEntityDetails(details),
    answered = answered,
)

fun CardEntity.toNewDbRecord(ids: IdSequences): Card {
    requireNew(this)
    val cardId = ids.nextCardId()
    val dictionaryId = dictionaryId.asDbRecordId()
    return Card(
        id = cardId,
        dictionaryId = dictionaryId,
        text = word,
        transcription = transcription,
        translations = translations.map {
            Translation(id = ids.nextTranslationId(), cardId = cardId, text = toDbRecordTranslations(it))
        },
        examples = examples.map {
            Example(id = ids.nextExampleId(), cardId = cardId, text = it)
        },
        partOfSpeech = partOfSpeech,
        details = toDbRecordDetails(details),
        answered = answered,
    )
}

private fun Long.asDictionaryId() = DictionaryId(toString())

private fun Long.asCardId() = CardId(toString())

private fun DictionaryId.asDbRecordId() = asString().toLong()