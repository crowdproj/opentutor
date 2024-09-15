package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppConfig
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetSettingsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SettingsResource
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateSettingsRequest
import com.gitlab.sszuev.flashcards.model.domain.SettingsEntity

fun SettingsContext.fromSettingsTransport(request: BaseRequest) = when (request) {
    is GetSettingsRequest -> fromGetSettingsRequest(request)
    is UpdateSettingsRequest -> fromUpdateSettingsRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass.simpleName}")
}

fun SettingsContext.fromGetSettingsRequest(request: GetSettingsRequest) {
    this.requestId = request.requestId()
}

fun SettingsContext.fromUpdateSettingsRequest(request: UpdateSettingsRequest) {
    this.requestId = request.requestId()
    this.requestSettingsEntity = request.settings?.toSettingsEntity(config) ?: SettingsEntity.DEFAULT
}

fun SettingsResource.toSettingsEntity(config: AppConfig) = SettingsEntity(
    numberOfWordsPerStage = this.numberOfWordsPerStage ?: config.defaultNumberOfWordsPerStage,
    stageShowNumberOfWords = this.stageShowNumberOfWords ?: config.defaultStageShowNumberOfWords,
    stageOptionsNumberOfVariants = this.stageOptionsNumberOfVariants ?: config.defaultStageOptionsNumberOfVariants,
)