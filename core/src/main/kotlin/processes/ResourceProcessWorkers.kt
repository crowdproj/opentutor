package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

fun ChainDSL<CardContext>.processResource() = worker {
    this.name = "process audio resource request"
    process {
        val request = this.normalizedRequestTTSResourceGet
        val foundId = this.repositories.ttsClientRepository.findResourceId(request)
        if (foundId.errors.isNotEmpty()) {
            this.errors.addAll(foundId.errors)
            this.status = AppStatus.FAIL
            return@process
        } else if (foundId.id == TTSResourceId.NONE) {
            this.errors.add(
                runError(
                    operation = CardOperation.GET_RESOURCE,
                    fieldName = this.requestTTSResourceGet.toFieldName(),
                    description = "no resource found. filter=$request"
                )
            )
            this.status = AppStatus.FAIL
            return@process
        }
        val foundResource = this.repositories.ttsClientRepository.getResource(foundId.id)
        if (foundResource.errors.isNotEmpty()) {
            this.errors.addAll(foundId.errors)
            this.status = AppStatus.FAIL
            return@process
        } else if (foundResource.resource == ResourceEntity.DUMMY) {
            this.errors.add(
                runError(
                    operation = CardOperation.GET_RESOURCE,
                    fieldName = foundId.id.toFieldName(),
                    description = "no resource found. id=${foundId.id}"
                )
            )
            this.status = AppStatus.FAIL
            return@process
        } else if (foundResource.resource.data.isEmpty()) {
            this.errors.add(
                runError(
                    operation = CardOperation.GET_RESOURCE,
                    fieldName = foundId.id.toFieldName(),
                    description = "empty resource found. id=${foundId.id}"
                )
            )
            this.status = AppStatus.FAIL
            return@process
        }
        this.responseTTSResourceEntity = foundResource.resource
        this.status = AppStatus.RUN
    }
    onException {
        fail(
            runError(
                operation = CardOperation.GET_RESOURCE,
                fieldName = this.requestTTSResourceGet.toFieldName(),
                description = "unexpected exception",
                exception = it
            )
        )
    }
}
