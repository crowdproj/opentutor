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
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun CardEntity.normalize(): CardEntity {
    return CardEntity(
        cardId = CardId(this.cardId.asString().trim()),
        dictionaryId = DictionaryId(this.dictionaryId.asString().trim()),
        word = this.word.trim()
    )
}

fun CardFilter.normalize(): CardFilter {
    return CardFilter(
        dictionaryIds = this.dictionaryIds.map { DictionaryId(it.asString().trim()) },
        random = this.random,
        length = this.length,
        withUnknown = this.withUnknown,
    )
}

fun ChainDSL<CardContext>.validateCardEntityCardId(getCardEntity: (CardContext) -> CardEntity) = worker {
    validateCardId { getCardEntity(it).cardId }
}

fun ChainDSL<CardContext>.validateCardEntityDictionaryId(getCardEntity: (CardContext) -> CardEntity) = worker {
    validateDictionaryId { getCardEntity(it).dictionaryId }
}

fun ChainDSL<CardContext>.validateCardId(getCardId: (CardContext) -> CardId) = worker {
    validateIdIsNotBlank(name = "Test card-id length", field = "card-id", getId = getCardId)
    validateIdMatchPattern(name = "Test card-id pattern", field = "card-id", getId = getCardId)
}

fun ChainDSL<CardContext>.validateDictionaryId(getDictionaryId: (CardContext) -> DictionaryId) = worker {
    validateIdIsNotBlank(name = "Test dictionary-id length", field = "dictionary-id", getId = getDictionaryId)
    validateIdMatchPattern(name = "Test dictionary-id pattern", field = "dictionary-id", getId = getDictionaryId)
}

fun ChainDSL<CardContext>.validateCardEntityWord(getCard: (CardContext) -> CardEntity) = worker {
    this.name = "Test card-word"
    test { isWordWrong(getCard(this).word) }
    process {
        fail(validationError(field = "card-word"))
    }
}

fun ChainDSL<CardContext>.validateIdIsNotBlank(
    name: String,
    field: String,
    getId: (CardContext) -> Id
) = worker {
    this.name = name
    test {
        isIdBlank(getId(this))
    }
    process {
        fail(validationError(field = field, description = "it is blank"))
    }
}

fun ChainDSL<CardContext>.validateIdMatchPattern(
    name: String,
    field: String,
    getId: (CardContext) -> Id
) = worker {
    this.name = name
    test {
        val id = getId(this)
        !isIdBlank(id) && isIdWrong(id)
    }
    process {
        fail(validationError(field = field, description = "must be integer number"))
    }
}

fun ChainDSL<CardContext>.validateCardFilterLength(getCardFilter: (CardContext) -> CardFilter) = worker {
    this.name = "Test card-filter length"
    test {
        getCardFilter(this).length <= 0
    }
    process {
        fail(validationError(field = "card-filter-length", description = "must be greater zero"))
    }
}

fun ChainDSL<CardContext>.validateCardFilterDictionaryIds(getCardFilter: (CardContext) -> CardFilter) = worker {
    validateIds(
        name = "Test card-filter dictionaryIds",
        field = "card-filter-dictionary-ids"
    ) {
        getCardFilter(it).dictionaryIds
    }
}

fun ChainDSL<CardContext>.validateIds(
    name: String,
    field: String,
    getIds: (CardContext) -> List<Id>
) = chain {
    worker {
        this.name = "$name:: length"
        test {
            getIds(this).isEmpty()
        }
        process {
            fail(validationError(field = field, description = "not specified"))
        }
    }
    worker {
        this.name = "$name:: content"
        process {
            this.errors.addAll(
                getIds(this)
                    .filter {
                        isIdBlank(it) || isIdWrong(it)
                    }.map {
                        validationError(field = field, description = "wrong id: $it")
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
    field: String,
    description: String = ""
) = AppError(
    code = "validation-$field",
    field = field,
    group = "validation",
    message = if (description.isBlank()) "" else "validation error for $field: $description"
)

internal fun CardContext.fail(error: AppError) {
    this.status = AppStatus.FAIL
    this.errors.add(error)
}