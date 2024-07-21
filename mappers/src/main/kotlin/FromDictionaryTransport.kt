package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.DownloadDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UploadDictionaryRequest
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

fun DictionaryContext.fromDictionaryTransport(request: BaseRequest) = when (request) {
    is GetAllDictionariesRequest -> fromGetAllDictionariesRequest(request)
    is CreateDictionaryRequest -> fromCreateDictionaryRequest(request)
    is DeleteDictionaryRequest -> fromDeleteDictionaryRequest(request)
    is DownloadDictionaryRequest -> fromDownloadDictionaryRequest(request)
    is UploadDictionaryRequest -> fromUploadDictionaryRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass.simpleName}")
}

fun DictionaryContext.fromGetAllDictionariesRequest(request: GetAllDictionariesRequest) {
    this.requestId = request.requestId()
}

fun DictionaryContext.fromCreateDictionaryRequest(request: CreateDictionaryRequest) {
    this.requestId = request.requestId()
    this.requestDictionaryEntity = request.dictionary?.toDictionaryEntity() ?: DictionaryEntity.EMPTY
}

fun DictionaryContext.fromDeleteDictionaryRequest(request: DeleteDictionaryRequest) {
    this.requestId = request.requestId()
    this.requestDictionaryId = toDictionaryId(request.dictionaryId)
}

fun DictionaryContext.fromDownloadDictionaryRequest(request: DownloadDictionaryRequest) {
    this.requestId = request.requestId()
    this.requestDictionaryId = toDictionaryId(request.dictionaryId)
    this.requestDownloadDocumentType = request.type ?: ""
}

fun DictionaryContext.fromUploadDictionaryRequest(request: UploadDictionaryRequest) {
    this.requestId = request.requestId()
    this.requestDictionaryResourceEntity = ResourceEntity(DictionaryId.NONE, request.resource ?: ByteArray(0))
    this.requestDownloadDocumentType = request.type ?: ""
}

fun DictionaryResource.toDictionaryEntity() = DictionaryEntity(
    dictionaryId = this.dictionaryId?.let { DictionaryId(it) } ?: DictionaryId.NONE,
    name = this.name ?: "",
    sourceLang = this.sourceLang?.let {
        LangEntity(langId = LangId(it), partsOfSpeech = this.partsOfSpeech ?: emptyList())
    } ?: LangEntity.EMPTY,
    targetLang = this.targetLang?.let { LangEntity(langId = LangId(it)) } ?: LangEntity.EMPTY,
    totalCardsCount = this.total ?: 0,
    learnedCardsCount = this.learned ?: 0,
)