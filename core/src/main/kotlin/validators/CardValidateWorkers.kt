package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn

fun ChainDSL<CardContext>.validateCardEntityHasValidCardId(getCardEntity: (CardContext) -> CardEntity) = worker {
    validateId("card-id") { getCardEntity(it).cardId }
}

fun ChainDSL<CardContext>.validateCardEntityHasNoCardId(getEntity: (CardContext) -> CardEntity) = worker {
    this.name = "Test card-id length"
    test {
        !isIdBlank(getEntity(this).cardId)
    }
    process {
        fail(validationError(fieldName = "card-id", description = "card-id is not expected"))
    }
}

fun ChainDSL<CardContext>.validateCardEntityDictionaryId(getCardEntity: (CardContext) -> CardEntity) = worker {
    validateDictionaryId { getCardEntity(it).dictionaryId }
}

fun ChainDSL<CardContext>.validateCardId(getCardId: (CardContext) -> CardId) = worker {
    validateId("card-id") { getCardId(it) }
}

fun ChainDSL<CardContext>.validateCardEntityWords(getCard: (CardContext) -> CardEntity) = worker {
    this.name = "Test card-word"
    test {
        val card = getCard(this)
        if (card.words.isEmpty()) {
            return@test true
        }
        if (card.words.any { !isCorrectWord(it.word) }) {
            return@test true
        }
        if (!isCorrectTranslation(card.words[0].translations)) {
            return@test true
        }
        val primaries = card.words.count { it.primary }
        return@test primaries != 0 && primaries != 1
    }
    process {
        fail(validationError(fieldName = "card-word"))
    }
}

fun ChainDSL<CardContext>.validateCardFilterLength(getCardFilter: (CardContext) -> CardFilter) = worker {
    this.name = "Test card-filter length"
    test {
        getCardFilter(this).length <= 0
    }
    process {
        fail(validationError(fieldName = "card-filter-length", description = "must be greater than zero"))
    }
}

fun ChainDSL<CardContext>.validateCardFilterDictionaryIds(getCardFilter: (CardContext) -> CardFilter) = validateIds(
    workerName = "Test card-filter dictionaryIds",
    fieldName = "card-filter-dictionary-ids"
) {
    getCardFilter(it).dictionaryIds
}

fun ChainDSL<CardContext>.validateCardLearnListCardIds(getCardLearn: (CardContext) -> List<CardLearn>) = validateIds(
    workerName = "Test learn-card ids",
    fieldName = "card-learn-card-ids"
) { context ->
    getCardLearn(context).map { it.cardId }
}

fun ChainDSL<CardContext>.validateCardLearnListStages(getCardLearn: (CardContext) -> List<CardLearn>) = validateFields(
    workerName = "Test learn-card stages",
    fieldName = "card-learn-stages",
    getEntityCollection = { context ->
        getCardLearn(context).map { cardLearn ->
            cardLearn.cardId to cardLearn.details.keys
        }
    }
) { (_, stages) ->
    stages.isEmpty()
}

fun ChainDSL<CardContext>.validateCardLearnListDetails(getCardLearn: (CardContext) -> List<CardLearn>) =
    validateCollectionFieldsAreCorrect(
        workerName = "Test learn-card details",
        fieldName = "card-learn-details",
        getEntityCollection = { context ->
            getCardLearn(context).flatMap { cardLearn ->
                cardLearn.details.values.map { value ->
                    cardLearn.cardId to value
                }
            }
        }
    ) { (_, score) ->
        // right not just check score is not big
        score < -42 || score > 42
    }

private fun ChainDSL<CardContext>.validateIds(
    workerName: String,
    fieldName: String,
    getIds: (CardContext) -> Collection<Id>
) = validateFields(workerName, fieldName, getIds) { isIdBlank(it) || isIdWrong(it) }

private fun <V> ChainDSL<CardContext>.validateFields(
    workerName: String,
    fieldName: String,
    getEntityCollection: (CardContext) -> Collection<V>,
    testIsWrong: (V) -> Boolean,
) = chain {
    name = "validate fields: fieldName=$fieldName"
    validateCollectionIsNotEmpty(workerName, fieldName, getEntityCollection)
    validateCollectionFieldsAreCorrect(workerName, fieldName, getEntityCollection, testIsWrong)
}

private fun <V> ChainDSL<CardContext>.validateCollectionIsNotEmpty(
    workerName: String,
    fieldName: String,
    getEntityCollection: (CardContext) -> Collection<V>,
) = worker {
    this.name = "$workerName:: length"
    test {
        getEntityCollection(this).isEmpty()
    }
    process {
        fail(validationError(fieldName = fieldName, description = "not specified"))
    }
}

private fun <V> ChainDSL<CardContext>.validateCollectionFieldsAreCorrect(
    workerName: String,
    fieldName: String,
    getEntityCollection: (CardContext) -> Collection<V>,
    testIsWrong: (V) -> Boolean = { false },
) = worker {
    this.name = "$workerName:: content"
    process {
        this.errors.addAll(
            getEntityCollection(this)
                .filter {
                    testIsWrong(it)
                }.map {
                    validationError(fieldName = fieldName, description = "wrong field value: [$it]")
                }.toList()
        )
        if (this.errors.isNotEmpty()) {
            this.status = AppStatus.FAIL
        }
    }
}