package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetSettingsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.SettingsResource
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateSettingsResponse
import com.gitlab.sszuev.flashcards.model.domain.SettingsEntity
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation

fun SettingsContext.toSettingsResponse(): BaseResponse = when (val op = this.operation) {
    SettingsOperation.GET_SETTINGS -> this.toGetSettingsResponse()
    SettingsOperation.UPDATE_SETTINGS -> this.toUpdateSettingsResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun SettingsContext.toGetSettingsResponse() = GetSettingsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    settings = this.responseSettingsEntity.toSettingsEntity()
)

fun SettingsContext.toUpdateSettingsResponse() = UpdateSettingsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun SettingsEntity.toSettingsEntity() = SettingsResource(
    numberOfWordsPerStage = this.numberOfWordsPerStage,
    stageShowNumberOfWords = this.stageShowNumberOfWords,
    stageOptionsNumberOfVariants = this.stageOptionsNumberOfVariants,
    stageMosaicSourceLangToTargetLang = this.stageMosaicSourceLangToTargetLang,
    stageOptionsSourceLangToTargetLang = this.stageOptionsSourceLangToTargetLang,
    stageWritingSourceLangToTargetLang = this.stageWritingSourceLangToTargetLang,
    stageSelfTestSourceLangToTargetLang = this.stageSelfTestSourceLangToTargetLang,
    stageMosaicTargetLangToSourceLang = this.stageMosaicTargetLangToSourceLang,
    stageOptionsTargetLangToSourceLang = this.stageOptionsTargetLangToSourceLang,
    stageWritingTargetLangToSourceLang = this.stageWritingTargetLangToSourceLang,
    stageSelfTestTargetLangToSourceLang = this.stageSelfTestTargetLangToSourceLang,
)