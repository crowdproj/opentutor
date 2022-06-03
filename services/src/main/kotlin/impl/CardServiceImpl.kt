package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.model.common.Error
import com.gitlab.sszuev.flashcards.model.common.Mode
import com.gitlab.sszuev.flashcards.model.common.Status
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.errorResponse
import com.gitlab.sszuev.flashcards.services.successResponse
import com.gitlab.sszuev.flashcards.stubs.createStubEntity

class CardServiceImpl : CardService {
    override fun createCardEntity(context: AppContext): AppContext {
        val response = when (context.workMode) {
            Mode.PROD -> TODO()
            Mode.TEST -> context.requestCardEntity
            Mode.STUB -> createStubEntity()
        }
        return when (context.status) {
            Status.OK -> context.successResponse { responseCardEntity = response }
            else -> context.errorResponse({ Error("unknown-error") })
        }
    }
}