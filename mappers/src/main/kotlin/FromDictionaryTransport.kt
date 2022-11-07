package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.*

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
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}

fun DictionaryContext.fromCreateDictionaryRequest(request: CreateDictionaryRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestDictionaryEntity = request.dictionary?.toDictionaryEntity() ?: DictionaryEntity.EMPTY
}

fun DictionaryContext.fromDeleteDictionaryRequest(request: DeleteDictionaryRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestDictionaryId = toDictionaryId(request.dictionaryId)
}

fun DictionaryContext.fromDownloadDictionaryRequest(request: DownloadDictionaryRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestDictionaryId = toDictionaryId(request.dictionaryId)
}

fun DictionaryContext.fromUploadDictionaryRequest(request: UploadDictionaryRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestDictionaryResourceEntity = ResourceEntity(DictionaryId.NONE, request.resource ?: ByteArray(0))
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