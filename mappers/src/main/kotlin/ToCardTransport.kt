package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardResponse
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun CardContext.toCardResponse(): BaseResponse = when (val op = this.operation) {
    CardOperation.GET_RESOURCE -> this.toGetAudioResponse()
    CardOperation.GET_ALL_CARDS -> this.toGetAllCardsResponse()
    CardOperation.SEARCH_CARDS -> this.toSearchCardsResponse()
    CardOperation.GET_CARD -> this.toGetCardResponse()
    CardOperation.CREATE_CARD -> this.toCreateCardResponse()
    CardOperation.UPDATE_CARD -> this.toUpdateCardResponse()
    CardOperation.DELETE_CARD -> this.toDeleteCardResponse()
    CardOperation.LEARN_CARDS -> this.toLearnCardResponse()
    CardOperation.RESET_CARD -> this.toResetCardResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun CardContext.toGetAudioResponse() = GetAudioResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    resource = this.responseTTSResourceEntity.data,
)

fun CardContext.toGetCardResponse() = GetCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    card = this.responseCardEntity.toCardResource()
)

fun CardContext.toGetAllCardsResponse() = GetAllCardsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() }
)

fun CardContext.toSearchCardsResponse() = SearchCardsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() }
)

fun CardContext.toCreateCardResponse() = CreateCardResponse(
    card = this.responseCardEntity.toCardResource(),
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toUpdateCardResponse() = UpdateCardResponse(
    card = this.responseCardEntity.toCardResource(),
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toDeleteCardResponse() = DeleteCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toLearnCardResponse() = LearnCardsResponse(
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() },
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toResetCardResponse() = ResetCardResponse(
    card = this.responseCardEntity.toCardResource(),
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

private fun CardEntity.toCardResource(): CardResource? { // TODO: change to bew model
    if (this == CardEntity.EMPTY) {
        return null
    }
    val word = this.words.first()
    return CardResource(
        cardId = this.cardId.takeIf { it != CardId.NONE }?.asString(),
        dictionaryId = this.dictionaryId.takeIf { it != DictionaryId.NONE }?.asString(),
        word = word.word,
        partOfSpeech = word.partOfSpeech,
        transcription = word.transcription,
        translations = word.translations,
        examples = word.examples.map { it.text },
        details = this.stats.mapKeys { it.key.name },
        answered = this.answered,
        sound = word.sound.asString().takeIf { it.isNotBlank() },
    )
}