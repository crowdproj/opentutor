package com.gitlab.sszuev.flashcards.logmappers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.logs.models.CardEntityResource
import com.gitlab.sszuev.flashcards.logs.models.CardFilterResource
import com.gitlab.sszuev.flashcards.logs.models.CardLearnResource
import com.gitlab.sszuev.flashcards.logs.models.CardsLogResource
import com.gitlab.sszuev.flashcards.logs.models.DictionariesLogResource
import com.gitlab.sszuev.flashcards.logs.models.DictionaryEntityResource
import com.gitlab.sszuev.flashcards.logs.models.ErrorLogResource
import com.gitlab.sszuev.flashcards.logs.models.LogResource
import com.gitlab.sszuev.flashcards.logs.models.UserLogResource
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import java.time.Instant
import java.util.UUID

fun AppContext.toLogResource(logId: String) = LogResource(
    source = "flashcards",
    messageId = UUID.randomUUID().toString(),
    messageTime = Instant.now().toString(),
    logId = logId,
    requestId = this.requestId.asString(),
    user = this.contextUserEntity.toLog(),
    cards = if (this is CardContext) this.toLog() else null,
    dictionaries = if (this is DictionaryContext) this.toLog() else null,
    errors = this.errors.takeIf { it.isNotEmpty() }?.map { it.toLog() }
)

private fun DictionaryContext.toLog() = DictionariesLogResource(
    responseDictionaries = this.responseDictionaryEntityList.takeIf { it.isNotEmpty() }?.map { it.toLog() }
)

private fun DictionaryEntity.toLog() = DictionaryEntityResource(
    dictionaryId = this.dictionaryId.asString(),
    name = this.name,
    partsOfSpeech = this.sourceLang.takeIf { it != LangEntity.EMPTY }?.partsOfSpeech?.takeIf { it.isNotEmpty() },
    sourceLang = this.sourceLang.takeIf { it != LangEntity.EMPTY }?.langId?.asString(),
    targetLang = this.targetLang.takeIf { it != LangEntity.EMPTY }?.langId?.asString(),
    learned = this.learnedCardsCount,
    total = this.totalCardsCount,
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

private fun CardEntity.toLog(): CardEntityResource { // TODO: change to new model
    val word = this.words.firstOrNull()
    return CardEntityResource(
        cardId = this.cardId.takeIf { it != CardId.NONE }?.asString(),
        dictionaryId = this.dictionaryId.asString(),
        word = word?.word?:"",
        transcription = word?.transcription,
        partOfSpeech = word?.partOfSpeech,
        details = this.stats.takeIf { it.isNotEmpty() }?.mapKeys { it.key.name },
        answered = this.answered,
        translations = word?.translations?.takeIf { it.isNotEmpty() },
        examples = word?.examples?.map { it.text }?.takeIf { it.isNotEmpty() },
        sound = word?.sound?.asString()?.takeIf { it.isNotBlank() },
    )
}

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