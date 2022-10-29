package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.*
import kotlinx.datetime.Instant

/**
 * Represents request context for card operations.
 */
data class CardContext(
    override val operation: CardOperation = CardOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val repositories: CardRepositories = CardRepositories.NO_OP_REPOSITORIES,
    override val errors: MutableList<AppError> = mutableListOf(),

    override var status: AppStatus = AppStatus.INIT,
    override var workMode: AppMode = AppMode.PROD,
    override var debugCase: AppStub = AppStub.NONE,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var contextUserEntity: AppUserEntity = AppUserEntity.EMPTY,

    // get word resource by id (for TTS)
    var requestResourceGet: TTSResourceGet = TTSResourceGet.NONE,
    var normalizedRequestResourceGet: TTSResourceGet = TTSResourceGet.NONE,
    // get word response (for TTS)
    var responseResourceEntity: ResourceEntity = ResourceEntity.DUMMY,

    // get-all cards request:
    var requestDictionaryId: DictionaryId = DictionaryId.NONE,
    var normalizedRequestDictionaryId: DictionaryId = DictionaryId.NONE,
    // get/delete single card request:
    var requestCardEntityId: CardId = CardId.NONE,
    var normalizedRequestCardEntityId: CardId = CardId.NONE,
    // get cards list request:
    var requestCardFilter: CardFilter = CardFilter.EMPTY,
    var normalizedRequestCardFilter: CardFilter = CardFilter.EMPTY,
    // update/create request:
    var requestCardEntity: CardEntity = CardEntity.EMPTY,
    var normalizedRequestCardEntity: CardEntity = CardEntity.EMPTY,
    // learn card
    var requestCardLearnList: List<CardLearn> = listOf(),
    var normalizedRequestCardLearnList: List<CardLearn> = listOf(),

    // get single card response:
    var responseCardEntity: CardEntity = CardEntity.EMPTY,
    // get cards list response:
    var responseCardEntityList: List<CardEntity> = listOf(),
): AppContext

