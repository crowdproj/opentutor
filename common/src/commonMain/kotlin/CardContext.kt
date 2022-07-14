package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.model.repositories.DummyTTSResourceRepository
import com.gitlab.sszuev.flashcards.model.repositories.TTSResourceRepository
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

    // TTS-service repo
    var ttsResourceRepository: TTSResourceRepository = DummyTTSResourceRepository,

    // get word resource by id (for TTS)
    var requestResourceGet: ResourceGet = ResourceGet.NONE,
    var normalizedRequestResourceGet: ResourceGet = ResourceGet.NONE,
    // get word response (for TTS)
    var responseResourceEntity: ResourceEntity = ResourceEntity.DUMMY,

    // get/delete single card request:
    var requestCardEntityId: CardId = CardId.NONE,
    var normalizedRequestCardEntityId: CardId = CardId.NONE,
    // get cards list request:
    var requestCardFilter: CardFilter = CardFilter.EMPTY,
    var normalizedRequestCardFilter: CardFilter = CardFilter.EMPTY,
    // update/create request:
    var requestCardEntity: CardEntity = CardEntity.DUMMY,
    var normalizedRequestCardEntity: CardEntity = CardEntity.DUMMY,
    // learn card
    var requestCardLearnList: List<CardLearn> = listOf(),
    var normalizedRequestCardLearnList: List<CardLearn> = listOf(),

    // get single card response:
    var responseCardEntity: CardEntity = CardEntity.DUMMY,
    // get cards list response:
    var responseCardEntityList: List<CardEntity> = listOf(),
)