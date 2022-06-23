package com.gitlab.sszuev.flashcards.core.validation

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.fail
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun CardEntity.normalize(): CardEntity {
    return CardEntity(
        cardId = CardId(cardId.asString().trim()),
        dictionaryId = DictionaryId(dictionaryId.asString().trim()),
        word = word.trim()
    )
}

fun ChainDSL<CardContext>.validateCardId(getCardId: (CardContext) -> CardId) = worker {
    this.name = "Test card-id"
    test { isBlankId(getCardId(this)) }
    process {
        fail(validationError(field = "card-id"))
    }
}

fun ChainDSL<CardContext>.validateDictionaryId(getDictionaryId: (CardContext) -> DictionaryId) = worker {
    this.name = "Test dictionary-id"
    test {
        isBlankId(getDictionaryId(this))
    }
    process {
        fail(validationError(field = "dictionary-id"))
    }
}

fun ChainDSL<CardContext>.validateCardWord(getCard: (CardContext) -> CardEntity) = worker {
    this.name = "Test card-word"
    test { isWordWrong(getCard(this).word) }
    process {
        fail(validationError(field = "card-word"))
    }
}

private fun isBlankId(id: Id): Boolean {
    return id.asString().isBlank()
}

/**
 * Tests word. See [wiki: Longest words](https://en.wikipedia.org/wiki/Longest_words).
 * @param [txt] to test
 * @return [Boolean]
 */
private fun isWordWrong(txt: String): Boolean {
    return txt.isBlank() || txt.length > 256
}

private fun validationError(field: String) = AppError(
    code = "validation-$field",
    field = field,
    group = "validation",
)
