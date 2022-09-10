package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesResponse
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation

fun DictionaryContext.toResponse(): BaseResponse = when (val op = this.operation) {
    DictionaryOperation.GET_ALL_DICTIONARIES -> this.toGetAllDictionaryResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun DictionaryContext.toGetAllDictionaryResponse() = GetAllDictionariesResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    dictionaries = this.responseDictionaryEntityList.mapNotNull { it.toDictionaryResource() }
)

private fun DictionaryEntity.toDictionaryResource(): DictionaryResource? {
    if (this == DictionaryEntity.EMPTY) {
        return null
    }
    return DictionaryResource(
        dictionaryId = this.dictionaryId.asString(),
        name = this.name,
        sourceLang = this.sourceLangId.toResponseId(),
        targetLang = this.targetLangId.toResponseId(),
        partsOfSpeech = this.partsOfSpeech,
        total = this.totalCardsCount,
        learned = this.learnedCardsCount,
    )
}
