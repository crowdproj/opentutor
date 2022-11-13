package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId

fun ChainDSL<DictionaryContext>.validateDictionaryResource() = worker {
    this.name = "Test upload dictionary resource"
    test {
        this.requestDictionaryResourceEntity.data.size < 300
    }
    process {
        fail(
            validationError(
                fieldName = "dictionary-resource",
                description = "uploaded byte-array is suspicious small: ${this.requestDictionaryResourceEntity.data.size} bytes"
            )
        )
    }
}

fun ChainDSL<DictionaryContext>.validateDictionaryEntityHasNoCardId(getEntity: (DictionaryContext) -> DictionaryEntity) =
    worker {
        this.name = "Test dictionary-id length"
        test {
            !isIdBlank(getEntity(this).dictionaryId)
        }
        process {
            fail(validationError(fieldName = "dictionary-id", description = "dictionary-id is not expected"))
        }
    }

fun ChainDSL<DictionaryContext>.validateDictionaryLangId(
    filedName: String,
    getLang: (DictionaryContext) -> LangId,
) = worker {
    this.name = "validate dictionary lang id"
    test {
        !isCorrectLangId(getLang(this).asString())
    }
    process {
        fail(validationError(fieldName = filedName, description = "invalid dictionary lang-id"))
    }
}