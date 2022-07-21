package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

fun ChainDSL<CardContext>.processResourceRequest() = worker {
    this.name = "process audio resource request"
    process {
        val request = this.normalizedRequestResourceGet
        val id = this.repositories.ttsClient.findResourceId(request.word, request.lang)
            ?: return@process fail(
                runError(
                    operation = CardOperation.GET_RESOURCE,
                    fieldName = this.requestResourceGet.toFieldName(),
                    description = "no resource found"
                )
            )
        this.responseResourceEntity = this.repositories.ttsClient.getResource(id)
        this.status = AppStatus.OK
    }
    onException {
        fail(
            runError(
                operation = CardOperation.GET_RESOURCE,
                fieldName = this.requestResourceGet.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}
