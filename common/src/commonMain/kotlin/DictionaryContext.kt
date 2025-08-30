package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class DictionaryContext(
    override val operation: DictionaryOperation = DictionaryOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val errors: MutableList<AppError> = mutableListOf(),
    override val config: AppConfig = AppConfig.DEFAULT,

    override var status: AppStatus = AppStatus.INIT,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,

    @Transient
    var repositories: DbRepositories = DbRepositories.NO_OP_REPOSITORIES,
    // get all dictionaries' list response:
    var responseDictionaryEntityList: List<DictionaryEntity> = listOf(),
    // update/create request:
    var requestDictionaryEntity: DictionaryEntity = DictionaryEntity.EMPTY,
    var normalizedRequestDictionaryEntity: DictionaryEntity = DictionaryEntity.EMPTY,
    // get-dictionary & delete-dictionary & download request:
    var requestDictionaryId: DictionaryId = DictionaryId.NONE,
    var normalizedRequestDictionaryId: DictionaryId = DictionaryId.NONE,
    // download & upload requests
    var requestDownloadDocumentType: String = "",
    var normalizedRequestDownloadDocumentType: String = "",
    // download-dictionary
    var responseDictionaryResourceEntity: ResourceEntity = ResourceEntity.DUMMY,
    // upload-dictionary
    var requestDictionaryResourceEntity: ResourceEntity = ResourceEntity.DUMMY,
    // upload/create/update dictionary response
    var responseDictionaryEntity: DictionaryEntity = DictionaryEntity.EMPTY,
    // used on first login to determine which dictionaries should be shown
    var userLanguage: String? = null,
) : AppContext