package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker

fun ChainDSL<CardContext>.validateResourceGetLangId() = worker {
    this.name = "validate get resource lang id"
    test {
        !isCorrectLangId(this.normalizedRequestTTSResourceGet.lang.asString())
    }
    process {
        fail(validationError(fieldName = "audio-resource-lang-id", description = "invalid resource lang-id"))
    }
}

fun ChainDSL<CardContext>.validateResourceGetWord() = worker {
    this.name = "validate get resource word"
    test {
        !isCorrectWrong(this.normalizedRequestTTSResourceGet.word)
    }
    process {
        fail(validationError(fieldName = "audio-resource-word", description = "invalid resource word"))
    }
}