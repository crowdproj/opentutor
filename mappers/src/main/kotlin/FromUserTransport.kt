package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext

fun AppContext.fromTransportToUser(request: String) {
    this.requestAppAuthId = AppAuthId(request)
}