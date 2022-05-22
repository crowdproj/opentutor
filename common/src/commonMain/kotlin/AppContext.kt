package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
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

    var requestEntity: CardEntity = CardEntity(),
    var responseEntity: CardEntity = CardEntity(),
)
