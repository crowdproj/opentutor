package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.*
import kotlinx.datetime.Instant

/**
 * Represents request context for card operations.
 */
data class CardContext(
    var operation: CardOperation = CardOperation.NONE,
    var status: AppStatus = AppStatus.INIT,
    val errors: MutableList<AppError> = mutableListOf(),

    var workMode: AppMode = AppMode.PROD,
    var debugCase: AppStub = AppStub.NONE,

    var requestId: AppRequestId = AppRequestId.NONE,
    var timestamp: Instant = Instant.NONE,

    // get/delete single card request:
    var requestCardEntityId: CardId = CardId.NONE,
    // get cards list request:
    var requestCardFilter: CardFilter = CardFilter.EMPTY,
    // update/create request:
    var requestCardEntity: CardEntity = CardEntity.DUMMY,
    var normalizedRequestCardEntity: CardEntity = CardEntity.DUMMY,
    // learn card
    var requestCardLearnList: List<CardLearn> = listOf(),

    // get single card response:
    var responseCardEntity: CardEntity = CardEntity.DUMMY,
    // get cards list response:
    var responseCardEntityList: List<CardEntity> = listOf(),
)