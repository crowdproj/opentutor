package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import kotlinx.datetime.Instant

/**
 * Represents request context for card operations.
 */
data class CardContext(
    override val operation: CardOperation = CardOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val repositories: AppRepositories = AppRepositories.NO_OP_REPOSITORIES,
    override val errors: MutableList<AppError> = mutableListOf(),
    override val config: AppConfig = AppConfig.DEFAULT,

    override var status: AppStatus = AppStatus.INIT,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,

    // get word resource by id (for TTS)
    var requestTTSResourceGet: TTSResourceGet = TTSResourceGet.NONE,
    var normalizedRequestTTSResourceGet: TTSResourceGet = TTSResourceGet.NONE,
    // get word response (for TTS)
    var responseTTSResourceEntity: ResourceEntity = ResourceEntity.DUMMY,

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
) : AppContext

