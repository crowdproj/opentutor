package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation

fun DictionaryContext.toDictionaryResponse(): BaseResponse = when (val op = this.operation) {
    DictionaryOperation.GET_ALL_DICTIONARIES -> this.toGetAllDictionaryResponse()
    DictionaryOperation.DELETE_DICTIONARY -> this.toDeleteDictionaryResponse()
    DictionaryOperation.DOWNLOAD_DICTIONARY -> this.toDownloadDictionaryResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun DictionaryContext.toGetAllDictionaryResponse() = GetAllDictionariesResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    dictionaries = this.responseDictionaryEntityList.mapNotNull { it.toDictionaryResource() }
)

fun DictionaryContext.toDeleteDictionaryResponse() = DeleteDictionaryResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun DictionaryContext.toDownloadDictionaryResponse() = DownloadDictionaryResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    resource = this.responseDictionaryResourceEntity.data,
)

private fun DictionaryEntity.toDictionaryResource(): DictionaryResource? {
    if (this == DictionaryEntity.EMPTY) {
        return null
    }
    return DictionaryResource(
        dictionaryId = this.dictionaryId.asString(),
        name = this.name,
        sourceLang = this.sourceLang.langId.toResponseId(),
        targetLang = this.targetLang.langId.toResponseId(),
        partsOfSpeech = this.sourceLang.partsOfSpeech,
        total = this.totalCardsCount,
        learned = this.learnedCardsCount,
    )
}
