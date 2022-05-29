package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import kotlinx.datetime.Instant

/**
 * Represents request context.
 */
data class AppContext(
    var operation: Operation = Operation.NONE,
    var status: Status = Status.UNKNOWN,
    val errors: MutableList<Error> = mutableListOf(),

    var workMode: Mode = Mode.PROD,
    var debugCase: Stub = Stub.NONE,

    var requestId: RequestId = RequestId.NONE,
    var timestamp: Instant = Instant.NONE,

    // get/delete single card request:
    var requestCardEntityId: CardId = CardId.NONE,
    // get cards list request:
    var requestCardFilter: CardFilter = CardFilter.EMPTY,
    // update/create request:
    var requestCardEntity: CardEntity = CardEntity(),
    // learn card
    var requestCardLearnList: List<CardLearn> = listOf(),

    // get single card response:
    var responseCardEntity: CardEntity = CardEntity(),
    // get cards list response:
    var responseCardEntityList: List<CardEntity> = listOf(),
)
