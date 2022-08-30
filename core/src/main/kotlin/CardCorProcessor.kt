package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.core.process.*
import com.gitlab.sszuev.flashcards.core.stubs.stubError
import com.gitlab.sszuev.flashcards.core.stubs.stubSuccess
import com.gitlab.sszuev.flashcards.core.utils.normalize
import com.gitlab.sszuev.flashcards.core.validation.*
import com.gitlab.sszuev.flashcards.corlib.chain
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
                normalize(CardOperation.GET_RESOURCE)
                validators(CardOperation.GET_RESOURCE) {
                    validateUserId(CardOperation.GET_RESOURCE)
                    validateResourceGetLangId()
                    validateResourceGetWord()
                }
                runs(CardOperation.GET_RESOURCE) {
                    processFindUser(CardOperation.GET_RESOURCE)
                    processResource()
                    finish(CardOperation.GET_RESOURCE)
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
                normalize(CardOperation.SEARCH_CARDS)
                validators(CardOperation.SEARCH_CARDS) {
                    validateUserId(CardOperation.SEARCH_CARDS)
                    validateCardFilterLength { it.normalizedRequestCardFilter }
                    validateCardFilterDictionaryIds { it.normalizedRequestCardFilter }
                }
                runs(CardOperation.SEARCH_CARDS) {
                    processFindUser(CardOperation.SEARCH_CARDS)
                    processCardSearch()
                    finish(CardOperation.SEARCH_CARDS)
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
                normalize(CardOperation.GET_ALL_CARDS)
                validators(CardOperation.GET_ALL_CARDS) {
                    validateUserId(CardOperation.GET_ALL_CARDS)
                    validateDictionaryId { it.normalizedRequestDictionaryId }
                }
                runs(CardOperation.GET_ALL_CARDS) {
                    processFindUser(CardOperation.GET_ALL_CARDS)
                    processGetAllCards()
                    finish(CardOperation.GET_ALL_CARDS)
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
                normalize(CardOperation.CREATE_CARD)
                validators(CardOperation.CREATE_CARD) {
                    validateUserId(CardOperation.CREATE_CARD)
                    validateCardEntityHasNoCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
                runs(CardOperation.CREATE_CARD) {
                    processFindUser(CardOperation.CREATE_CARD)
                    processCreateCard()
                    finish(CardOperation.CREATE_CARD)
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
                normalize(CardOperation.UPDATE_CARD)
                validators(CardOperation.UPDATE_CARD) {
                    validateUserId(CardOperation.UPDATE_CARD)
                    validateCardEntityHasValidCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
                runs(CardOperation.UPDATE_CARD) {
                    processFindUser(CardOperation.UPDATE_CARD)
                    processUpdateCard()
                    finish(CardOperation.UPDATE_CARD)
                }
            }

            operation(CardOperation.LEARN_CARDS) {
                stubs(CardOperation.LEARN_CARDS) {
                    stubSuccess(CardOperation.LEARN_CARDS)
                    stubError(CardOperation.LEARN_CARDS)
                    stubError(CardOperation.LEARN_CARDS, AppStub.ERROR_LEARN_CARD_WRONG_CARD_ID)
                    stubError(CardOperation.LEARN_CARDS, AppStub.ERROR_LEARN_CARD_WRONG_STAGES)
                    stubError(CardOperation.LEARN_CARDS, AppStub.ERROR_LEARN_CARD_WRONG_DETAILS)
                }
                normalize(CardOperation.LEARN_CARDS)
                validators(CardOperation.LEARN_CARDS) {
                    validateUserId(CardOperation.LEARN_CARDS)
                    validateCardLearnListCardIds { it.normalizedRequestCardLearnList }
                    validateCardLearnListStages { it.normalizedRequestCardLearnList }
                    validateCardLearnListDetails { it.normalizedRequestCardLearnList }
                }
                runs(CardOperation.LEARN_CARDS) {
                    processFindUser(CardOperation.LEARN_CARDS)
                    processLearnCards()
                    finish(CardOperation.LEARN_CARDS)
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
                normalize(CardOperation.GET_CARD)
                validators(CardOperation.GET_CARD) {
                    validateUserId(CardOperation.GET_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.GET_CARD) {
                    processFindUser(CardOperation.GET_CARD)
                    processGetCard()
                    finish(CardOperation.GET_CARD)
                }
            }

            operation(CardOperation.RESET_CARD) {
                stubs(CardOperation.RESET_CARD) {
                    stubSuccess(CardOperation.RESET_CARD)
                    stubError(CardOperation.RESET_CARD)
                    stubError(CardOperation.RESET_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                normalize(CardOperation.RESET_CARD)
                validators(CardOperation.RESET_CARD) {
                    validateUserId(CardOperation.RESET_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.RESET_CARD) {
                    processFindUser(CardOperation.RESET_CARD)
                    processResetCards()
                    finish(CardOperation.RESET_CARD)
                }
            }
            operation(CardOperation.DELETE_CARD) {
                stubs(CardOperation.DELETE_CARD) {
                    stubSuccess(CardOperation.DELETE_CARD)
                    stubError(CardOperation.DELETE_CARD)
                    stubError(CardOperation.DELETE_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                normalize(CardOperation.DELETE_CARD)
                validators(CardOperation.DELETE_CARD) {
                    validateUserId(CardOperation.DELETE_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.DELETE_CARD) {
                    processFindUser(CardOperation.DELETE_CARD)
                    processDeleteCard()
                    finish(CardOperation.DELETE_CARD)
                }
            }
        }.build()
    }
}