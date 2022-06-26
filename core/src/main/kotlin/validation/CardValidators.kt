package com.gitlab.sszuev.flashcards.core.validation

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn

fun ChainDSL<CardContext>.validateCardEntityCardId(getCardEntity: (CardContext) -> CardEntity) = worker {
    validateId("card-id") { getCardEntity(it).cardId }
}

fun ChainDSL<CardContext>.validateCardEntityDictionaryId(getCardEntity: (CardContext) -> CardEntity) = worker {
    validateId("dictionary-id") { getCardEntity(it).dictionaryId }
}

fun ChainDSL<CardContext>.validateCardId(getCardId: (CardContext) -> CardId) = worker {
    validateId("card-id") { getCardId(it) }
}

fun ChainDSL<CardContext>.validateCardEntityWord(getCard: (CardContext) -> CardEntity) = worker {
    this.name = "Test card-word"
    test { isWordWrong(getCard(this).word) }
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
        fail(validationError(fieldName = "card-filter-length", description = "must be greater zero"))
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
        getCardLearn(context).flatMap { cardLearn ->
            cardLearn.details.keys.map { stage ->
                cardLearn.cardId to stage
            }
        }
    }
) { (_, stage) ->
    // stage is determined by some vocabulary (can be fixed in system).
    // right not just check it is not empty and not too long
    stage.isBlank() || stage.length > 10
}

fun ChainDSL<CardContext>.validateCardLearnListDetails(getCardLearn: (CardContext) -> List<CardLearn>) = validateFields(
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
    // right not just check score is positive and not big
    score <= 0 || score > 42
}

private fun ChainDSL<CardContext>.validateId(
    fieldName: String,
    getId: (CardContext) -> Id
) = chain {
    validateIdIsNotBlank(workerName = "Test $fieldName length", fieldName = fieldName, getId = getId)
    validateIdMatchPattern(workerName = "Test $fieldName pattern", fieldName = fieldName, getId = getId)
}

private fun ChainDSL<CardContext>.validateIdIsNotBlank(
    workerName: String,
    fieldName: String,
    getId: (CardContext) -> Id
) = worker {
    this.name = workerName
    test {
        isIdBlank(getId(this))
    }
    process {
        fail(validationError(fieldName = fieldName, description = "it is blank"))
    }
}

private fun ChainDSL<CardContext>.validateIdMatchPattern(
    workerName: String,
    fieldName: String,
    getId: (CardContext) -> Id
) = worker {
    this.name = workerName
    test {
        val id = getId(this)
        !isIdBlank(id) && isIdWrong(id)
    }
    process {
        fail(validationError(fieldName = fieldName, description = "must be integer number"))
    }
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
    worker {
        this.name = "$workerName:: length"
        test {
            getEntityCollection(this).isEmpty()
        }
        process {
            fail(validationError(fieldName = fieldName, description = "not specified"))
        }
    }
    worker {
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
}

private fun isIdBlank(id: Id): Boolean {
    return id.asString().isBlank()
}

private fun isIdWrong(id: Id): Boolean {
    return !id.asString().matches(Regex("\\d+"))
}

/**
 * Tests word. See [wiki: Longest words](https://en.wikipedia.org/wiki/Longest_words).
 * @param [txt] to test
 * @return [Boolean]
 */
private fun isWordWrong(txt: String): Boolean {
    return txt.isBlank() || txt.length > 256
}

private fun validationError(
    fieldName: String,
    description: String = ""
) = AppError(
    code = "validation-$fieldName",
    field = fieldName,
    group = "validation",
    message = if (description.isBlank()) "" else "validation error for $fieldName: $description"
)

internal fun CardContext.fail(error: AppError) {
    this.status = AppStatus.FAIL
    this.errors.add(error)
}