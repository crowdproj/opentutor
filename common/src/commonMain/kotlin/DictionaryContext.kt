package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import kotlinx.datetime.Instant

data class DictionaryContext(
    override val operation: DictionaryOperation = DictionaryOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val repositories: AppRepositories = AppRepositories.NO_OP_REPOSITORIES,
    override val errors: MutableList<AppError> = mutableListOf(),
    override val config: AppConfig = AppConfig.DEFAULT,

    override var status: AppStatus = AppStatus.INIT,
    override var workMode: AppMode = AppMode.PROD,
    override var debugCase: AppStub = AppStub.NONE,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,

    // get all dictionaries' list response:
    var responseDictionaryEntityList: List<DictionaryEntity> = listOf(),
    // update/create request:
    var requestDictionaryEntity: DictionaryEntity = DictionaryEntity.EMPTY,
    var normalizedRequestDictionaryEntity: DictionaryEntity = DictionaryEntity.EMPTY,
    // get-dictionary & delete-dictionary request:
    var requestDictionaryId: DictionaryId = DictionaryId.NONE,
    var normalizedRequestDictionaryId: DictionaryId = DictionaryId.NONE,
    // download-dictionary
    var responseDictionaryResourceEntity: ResourceEntity = ResourceEntity.DUMMY,
    // upload-dictionary
    var requestDictionaryResourceEntity: ResourceEntity = ResourceEntity.DUMMY,
    // upload/create/update dictionary response
    var responseDictionaryEntity: DictionaryEntity = DictionaryEntity.EMPTY,
) : AppContext