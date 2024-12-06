package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.domain.LangId

fun ChainDSL<TranslationContext>.validateLangId(
    filedName: String,
    getLang: (TranslationContext) -> LangId,
) = worker {
    this.name = "validate translation lang id"
    test {
        !isCorrectLangId(getLang(this).asString())
    }
    process {
        fail(
            validationError(
                fieldName = filedName,
                description = "invalid translation lang-id='${getLang(this).asString()}'"
            )
        )
    }
}

fun ChainDSL<TranslationContext>.validateQueryWord() = worker {
    this.name = "validate query word"
    test {
        !isCorrectQueryWord(this.requestWord)
    }
    process {
        fail(validationError(fieldName = "query-word", description = "invalid query word"))
    }
}

private fun isCorrectQueryWord(word: String): Boolean = isCorrectWord(word)

