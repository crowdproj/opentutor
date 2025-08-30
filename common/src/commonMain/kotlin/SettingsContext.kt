package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.SettingsEntity
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class SettingsContext(
    override val operation: SettingsOperation = SettingsOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val errors: MutableList<AppError> = mutableListOf(),
    override val config: AppConfig = AppConfig.DEFAULT,

    override var status: AppStatus = AppStatus.INIT,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,

    @Transient
    var repositories: DbRepositories = DbRepositories.NO_OP_REPOSITORIES,

    // update
    var requestSettingsEntity: SettingsEntity = SettingsEntity.DEFAULT,
    // get
    var responseSettingsEntity: SettingsEntity = SettingsEntity.DEFAULT,
) : AppContext