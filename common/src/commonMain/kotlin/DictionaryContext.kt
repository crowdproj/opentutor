package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.UserEntity
import com.gitlab.sszuev.flashcards.model.domain.UserUid
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

    // get user:
    var requestUserUid: UserUid = UserUid.NONE,
    var normalizedRequestUserUid: UserUid = UserUid.NONE,
    var contextUserEntity: UserEntity = UserEntity.EMPTY,

    // get all dictionaries list response:
    var responseDictionaryEntityList: List<DictionaryEntity> = listOf(),
): AppContext