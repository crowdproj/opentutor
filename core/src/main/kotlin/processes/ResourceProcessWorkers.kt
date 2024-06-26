package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation

fun ChainDSL<TTSContext>.processResource() = worker {
    this.name = "process audio resource request"
    process {
        val request = this.normalizedRequestTTSResourceGet

        val id = request.asResourceId()
        val found = this.repository.findResource(request.lang.asString(), request.word)
        if (found == null) {
            this.errors.add(
                runError(
                    operation = TTSOperation.GET_RESOURCE,
                    fieldName = id.toFieldName(),
                    description = "no resource found. filter=$request"
                )
            )
            this.status = AppStatus.FAIL
        } else {
            this.responseTTSResourceEntity = ResourceEntity(resourceId = id, data = found)
            this.status = AppStatus.RUN
        }
    }
    onException {
        fail(
            runError(
                operation = TTSOperation.GET_RESOURCE,
                fieldName = this.requestTTSResourceGet.toFieldName(),
                description = "unexpected exception",
                exception = it
            )
        )
    }
}
