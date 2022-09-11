package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import kotlinx.datetime.Instant

data class DictionaryContext(
    override val repositories: DictionaryRepositories = DictionaryRepositories.NO_OP_REPOSITORIES,
    override val operation: DictionaryOperation = DictionaryOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val errors: MutableList<AppError> = mutableListOf(),

    override var status: AppStatus = AppStatus.INIT,
    override var workMode: AppMode = AppMode.PROD,
    override var debugCase: AppStub = AppStub.NONE,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var contextUserEntity: AppUserEntity = AppUserEntity.EMPTY,

    // get all dictionaries list response:
    var responseDictionaryEntityList: List<DictionaryEntity> = listOf(),
): AppContext