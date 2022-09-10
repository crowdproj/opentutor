package com.gitlab.sszuev.flashcards.logmappers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.logs.models.*
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.domain.*
import java.time.Instant
import java.util.*

fun CardContext.toLogResource(logId: String) = LogResource(
    source = "flashcards",
    messageId = UUID.randomUUID().toString(),
    messageTime = Instant.now().toString(),
    logId = logId,
    requestId = this.requestId.asString(),
    user = this.contextUserEntity.toLog(),
    cards = this.toLog(),
    errors = this.errors.takeIf { it.isNotEmpty() }?.map { it.toLog() }
)

private fun AppUserEntity.toLog() = UserLogResource(
    userId = id.asString(),
    userUid = authId.asString(),
)

private fun AppError.toLog() = ErrorLogResource(
    code = this.code,
    field = this.field,
    group = this.group,
    message = this.message,
)

private fun CardContext.toLog() = CardsLogResource(
    requestCardId = this.requestCardEntityId.takeIf { it != CardId.NONE }?.asString(),
    requestDictionaryId = this.requestDictionaryId.takeIf { it != DictionaryId.NONE }?.asString(),
    requestCard = this.requestCardEntity.takeIf { it != CardEntity.EMPTY }?.toLog(),
    responseCard = this.responseCardEntity.takeIf { it != CardEntity.EMPTY }?.toLog(),
    requestCardFilter = this.requestCardFilter.takeIf { it != CardFilter.EMPTY }?.toLog(),
    requestCardLearn = this.requestCardLearnList.takeIf { it.isNotEmpty() }?.map { it.toLog() },
    responseCards = this.responseCardEntityList.takeIf { it.isNotEmpty() }?.map { it.toLog() },
)

private fun CardEntity.toLog() = CardEntityResource(
    cardId = this.cardId.takeIf { it != CardId.NONE }?.asString(),
    dictionaryId = this.dictionaryId.asString(),
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    details = this.details.takeIf { it.isNotEmpty() }?.mapKeys { it.key.name },
    answered = this.answered,
    translations = this.translations.takeIf { it.isNotEmpty() },
    examples = this.examples.takeIf { it.isNotEmpty() },
)

private fun CardFilter.toLog() = CardFilterResource(
    dictionaryIds = this.dictionaryIds.map { it.asString() },
    random = this.random,
    unknown = this.withUnknown,
    length = this.length,
)

private fun CardLearn.toLog() = CardLearnResource(
    cardId = this.cardId.asString(),
    details = this.details.mapKeys { it.key.name }
)