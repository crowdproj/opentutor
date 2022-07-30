package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.core.process.processGetAllCardsRequest
import com.gitlab.sszuev.flashcards.core.process.processResourceRequest
import com.gitlab.sszuev.flashcards.core.stubs.stubError
import com.gitlab.sszuev.flashcards.core.stubs.stubSuccess
import com.gitlab.sszuev.flashcards.core.utils.normalize
import com.gitlab.sszuev.flashcards.core.validation.*
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.stubs.stubAudioResource
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards

/**
 * Main class fot business logic,
 * it is based on Chain-Of-Responsibility (COR) pattern.
 */
class CardCorProcessor(private val repositories: CardRepositories) {

    suspend fun execute(context: CardContext) =
        businessChain.exec(context.apply { this.repositories = this@CardCorProcessor.repositories })

    companion object {
        private val businessChain = chain {
            initContext()

            operation(CardOperation.GET_RESOURCE) {
                stubs(CardOperation.GET_RESOURCE) {
                    stubSuccess(CardOperation.GET_RESOURCE) {
                        this.responseResourceEntity = stubAudioResource
                    }
                    stubError(CardOperation.GET_RESOURCE)
                    stubError(CardOperation.GET_RESOURCE, AppStub.ERROR_AUDIO_RESOURCE_WRONG_RESOURCE_ID)
                    stubError(CardOperation.GET_RESOURCE, AppStub.ERROR_AUDIO_RESOURCE_NOT_FOUND)
                    stubError(CardOperation.GET_RESOURCE, AppStub.ERROR_AUDIO_RESOURCE_SERVER_ERROR)
                }
                validators(CardOperation.GET_RESOURCE) {
                    worker(name = "Make a normalized copy of get-resource id") {
                        this.normalizedRequestResourceGet = this.requestResourceGet.normalize()
                    }
                    validateResourceGetLangId()
                    validateResourceGetWord()
                }
                runs(CardOperation.GET_RESOURCE) {
                    processResourceRequest()
                }
            }

            operation(CardOperation.SEARCH_CARDS) {
                stubs(CardOperation.SEARCH_CARDS) {
                    stubSuccess(CardOperation.SEARCH_CARDS) {
                        this.responseCardEntityList = stubCards
                    }
                    stubError(CardOperation.SEARCH_CARDS)
                    stubError(CardOperation.SEARCH_CARDS, AppStub.ERROR_CARDS_WRONG_FILTER_LENGTH)
                    stubError(CardOperation.SEARCH_CARDS, AppStub.ERROR_WRONG_DICTIONARY_ID)
                }
                validators(CardOperation.SEARCH_CARDS) {
                    worker(name = "Make a normalized copy of search-cards request") {
                        this.normalizedRequestCardFilter = this.requestCardFilter.normalize()
                    }
                    validateCardFilterLength { it.normalizedRequestCardFilter }
                    validateCardFilterDictionaryIds { it.normalizedRequestCardFilter }
                }
            }

            operation(CardOperation.GET_ALL_CARDS) {
                stubs(CardOperation.GET_ALL_CARDS) {
                    stubSuccess(CardOperation.GET_ALL_CARDS) {
                        this.responseCardEntityList = stubCards
                    }
                    stubError(CardOperation.GET_ALL_CARDS)
                    stubError(CardOperation.GET_ALL_CARDS, AppStub.ERROR_WRONG_DICTIONARY_ID)
                }
                validators(CardOperation.GET_ALL_CARDS) {
                    worker(name = "Make a normalized copy of get-all-cards request") {
                        this.normalizedRequestDictionaryId = this.requestDictionaryId.normalize()
                    }
                    validateDictionaryId { it.normalizedRequestDictionaryId }
                }
                runs(CardOperation.GET_ALL_CARDS) {
                    processGetAllCardsRequest()
                }
            }

            operation(CardOperation.CREATE_CARD) {
                stubs(CardOperation.CREATE_CARD) {
                    stubSuccess(CardOperation.CREATE_CARD) {
                        this.responseCardEntity = stubCard
                    }
                    stubError(CardOperation.CREATE_CARD)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_UNEXPECTED_FIELD)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_WORD)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_TRANSLATION)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_TRANSCRIPTION)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_EXAMPLES)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_DETAILS)
                    stubError(CardOperation.CREATE_CARD, AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE)
                }
                validators(CardOperation.CREATE_CARD) {
                    worker(name = "Make a normalized copy of create-card card-entity") {
                        this.normalizedRequestCardEntity = this.requestCardEntity.normalize()
                    }
                    validateCardEntityHasNoCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
            }

            operation(CardOperation.UPDATE_CARD) {
                stubs(CardOperation.UPDATE_CARD) {
                    stubSuccess(CardOperation.UPDATE_CARD) {
                        this.responseCardEntity = stubCard
                    }
                    stubError(CardOperation.UPDATE_CARD)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_WRONG_CARD_ID)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_WORD)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_TRANSLATION)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_TRANSCRIPTION)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_EXAMPLES)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_DETAILS)
                    stubError(CardOperation.UPDATE_CARD, AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE)
                }
                validators(CardOperation.UPDATE_CARD) {
                    worker(name = "Make a normalized copy of update-card card-entity") {
                        this.normalizedRequestCardEntity = this.requestCardEntity.normalize()
                    }
                    validateCardEntityHasValidCardId { it.normalizedRequestCardEntity }
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

            operation(CardOperation.RESET_CARD) {
                stubs(CardOperation.RESET_CARD) {
                    stubSuccess(CardOperation.RESET_CARD)
                    stubError(CardOperation.RESET_CARD)
                    stubError(CardOperation.RESET_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                validators(CardOperation.RESET_CARD) {
                    worker(name = "Make a normalized copy of reset-card-id") {
                        this.normalizedRequestCardEntityId = this.requestCardEntityId.normalize()
                    }
                    validateCardId { it.normalizedRequestCardEntityId }
                }
            }
            operation(CardOperation.DELETE_CARD) {
                stubs(CardOperation.DELETE_CARD) {
                    stubSuccess(CardOperation.DELETE_CARD)
                    stubError(CardOperation.DELETE_CARD)
                    stubError(CardOperation.DELETE_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                validators(CardOperation.DELETE_CARD) {
                    worker(name = "Make a normalized copy of delete-card-id") {
                        this.normalizedRequestCardEntityId = this.requestCardEntityId.normalize()
                    }
                    validateCardId { it.normalizedRequestCardEntityId }
                }
            }
        }.build()
    }
}