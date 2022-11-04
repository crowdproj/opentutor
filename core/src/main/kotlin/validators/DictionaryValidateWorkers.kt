package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker

fun ChainDSL<DictionaryContext>.validateDictionaryResource() = worker {
    this.name = "Test upload dictionary resource"
    test {
        this.requestDictionaryResourceEntity.data.size < 1000
    }
    process {
        fail(validationError(fieldName = "dictionary-resource", description = "uploaded byte-array is suspicious small"))
    }
}