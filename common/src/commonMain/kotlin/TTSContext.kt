package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.repositories.NoOpTTSResourceRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TTSContext(
    override val operation: TTSOperation = TTSOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val errors: MutableList<AppError> = mutableListOf(),
    override val config: AppConfig = AppConfig.DEFAULT,

    override var status: AppStatus = AppStatus.INIT,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,

    @Transient
    var repository: TTSResourceRepository = NoOpTTSResourceRepository,

    // get word resource by id (for TTS)
    var requestTTSResourceGet: TTSResourceGet = TTSResourceGet.NONE,
    var normalizedRequestTTSResourceGet: TTSResourceGet = TTSResourceGet.NONE,
    // get word response (for TTS)
    var responseTTSResourceEntity: ResourceEntity = ResourceEntity.DUMMY,
) : AppContext
