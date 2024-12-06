package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation
import com.gitlab.sszuev.flashcards.translation.api.NoOpTranslationRepository
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TranslationContext(
    override val operation: TranslationOperation = TranslationOperation.NONE,
    override val timestamp: Instant = Instant.NONE,
    override val errors: MutableList<AppError> = mutableListOf(),
    override val config: AppConfig = AppConfig.DEFAULT,

    override var status: AppStatus = AppStatus.INIT,
    override var requestId: AppRequestId = AppRequestId.NONE,
    override var requestAppAuthId: AppAuthId = AppAuthId.NONE,
    override var normalizedRequestAppAuthId: AppAuthId = AppAuthId.NONE,

    @Transient
    var repository: TranslationRepository = NoOpTranslationRepository,

    var requestWord: String = "",
    var requestSourceLang: LangId = LangId.NONE,
    var requestTargetLang: LangId = LangId.NONE,

    var normalizedRequestWord: String = "",
    var normalizedRequestSourceLang: LangId = LangId.NONE,
    var normalizedRequestTargetLang: LangId = LangId.NONE,

    var responseCardEntity: CardEntity = CardEntity.EMPTY,
) : AppContext