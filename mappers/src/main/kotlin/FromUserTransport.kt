package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext

fun AppContext.fromUserTransport(request: String) {
    this.requestAppAuthId = AppAuthId(request)
}

fun AppContext.fromUserTransportIfRequired(request: () -> String) {
    if (this.requestAppAuthId == AppAuthId.NONE) {
        fromUserTransport(request())
    }
}