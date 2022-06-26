package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.stubs.stubError
import com.gitlab.sszuev.flashcards.core.stubs.stubSuccess
import com.gitlab.sszuev.flashcards.core.validation.*
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards

/**
 * Main class fot business logic,
 * it is based on Chain-Of-Responsibility (COR) pattern.
 */
class CardCorProcessor {

    suspend fun execute(context: CardContext) = businessChain.build().exec(context)

    companion object {
        private val businessChain = chain {
            initContext()

            operation(CardOperation.SEARCH_CARDS) {
                stubs(CardOperation.SEARCH_CARDS) {
                    stubSuccess(CardOperation.SEARCH_CARDS) {
                        this.responseCardEntityList = stubCards
                    }
                    stubError(CardOperation.SEARCH_CARDS)
                    stubError(CardOperation.SEARCH_CARDS, AppStub.ERROR_CARDS_FILTER_WRONG_LENGTH)
                    stubError(CardOperation.SEARCH_CARDS, AppStub.ERROR_CARDS_FILTER_WRONG_DICTIONARY_ID)
                }
                validators(CardOperation.SEARCH_CARDS) {
                    worker(name = "Make a normalized copy of search-cards request") {
                        this.normalizedRequestCardFilter = this.requestCardFilter.normalize()
                    }
                    validateCardFilterLength { it.normalizedRequestCardFilter }
                    validateCardFilterDictionaryIds { it.normalizedRequestCardFilter }
                }
            }

            operation(CardOperation.GET_CARD) {
                stubs(CardOperation.GET_CARD) {
                    stubSuccess(CardOperation.GET_CARD) {
                        this.responseCardEntity = stubCard
                    }
                    stubError(CardOperation.GET_CARD)
                    stubError(CardOperation.GET_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                validators(CardOperation.GET_CARD) {
                    worker(name = "Make a normalized copy of get-card-id") {
                        this.normalizedRequestCardEntityId = this.requestCardEntityId.normalize()
                    }
                    validateCardId { it.normalizedRequestCardEntityId }
                }
            }

            operation(CardOperation.CREATE_CARD) {
                stubs(CardOperation.CREATE_CARD) {
                    stubSuccess(CardOperation.CREATE_CARD) {
                        this.responseCardEntity = stubCard
                    }
                    stubError(CardOperation.CREATE_CARD)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_WRONG_CARD_ID)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_WORD)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_TRANSLATION)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_TRANSCRIPTION)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_EXAMPLES)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_DETAILS)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE)
                }
                validators(CardOperation.CREATE_CARD) {
                    worker(name = "Make a normalized copy of get-card request") {
                        this.normalizedRequestCardEntity = this.requestCardEntity.normalize()
                    }
                    validateCardEntityCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
            }

            operation(CardOperation.LEARN_CARD) {
                stubs(CardOperation.LEARN_CARD) {
                    stubSuccess(CardOperation.LEARN_CARD)
                    stubError(CardOperation.LEARN_CARD)
                    stubError(CardOperation.LEARN_CARD, AppStub.ERROR_LEARN_CARD_WRONG_CARD_ID)
                    stubError(CardOperation.LEARN_CARD, AppStub.ERROR_LEARN_CARD_WRONG_STAGES)
                    stubError(CardOperation.LEARN_CARD, AppStub.ERROR_LEARN_CARD_WRONG_DETAILS)
                }
                validators(CardOperation.LEARN_CARD) {
                    worker(name = "Make a normalized copy of learn-card request") {
                        this.normalizedRequestCardLearnList = this.requestCardLearnList.map { it.normalize() }
                    }
                    validateCardLearnListCardIds { it.normalizedRequestCardLearnList }
                    validateCardLearnListStages { it.normalizedRequestCardLearnList }
                    validateCardLearnListDetails { it.normalizedRequestCardLearnList }
                }
            }
        }
    }
}