package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.CreateDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.DownloadDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UploadDictionaryResponse
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation

fun DictionaryContext.toDictionaryResponse(): BaseResponse = when (val op = this.operation) {
    DictionaryOperation.GET_ALL_DICTIONARIES -> this.toGetAllDictionaryResponse()
    DictionaryOperation.CREATE_DICTIONARY -> this.toCreateDictionaryResponse()
    DictionaryOperation.UPDATE_DICTIONARY -> this.toUpdateDictionaryResponse()
    DictionaryOperation.DELETE_DICTIONARY -> this.toDeleteDictionaryResponse()
    DictionaryOperation.DOWNLOAD_DICTIONARY -> this.toDownloadDictionaryResponse()
    DictionaryOperation.UPLOAD_DICTIONARY -> this.toUploadDictionaryResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun DictionaryContext.toGetAllDictionaryResponse() = GetAllDictionariesResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    dictionaries = this.responseDictionaryEntityList.mapNotNull { it.toDictionaryResource() }
)

fun DictionaryContext.toCreateDictionaryResponse() = CreateDictionaryResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    dictionary = this.responseDictionaryEntity.toDictionaryResource(),
)

fun DictionaryContext.toUpdateDictionaryResponse() = UpdateDictionaryResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    dictionary = this.responseDictionaryEntity.toDictionaryResource(),
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

fun DictionaryContext.toUploadDictionaryResponse() = UploadDictionaryResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    dictionary = this.responseDictionaryEntity.toDictionaryResource(),
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
        numberOfRightAnswers = this.numberOfRightAnswers,
    )
}
