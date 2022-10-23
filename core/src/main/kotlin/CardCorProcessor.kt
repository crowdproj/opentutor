package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.*
import com.gitlab.sszuev.flashcards.core.stubs.cardStubSuccess
import com.gitlab.sszuev.flashcards.core.stubs.stubError
import com.gitlab.sszuev.flashcards.core.validators.*
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
class CardCorProcessor {

    suspend fun execute(context: CardContext) = businessChain.exec(context)

    companion object {
        private val businessChain = chain {
            name = "CardContext Root Chain"
            initContext()

            operation(CardOperation.GET_RESOURCE) {
                stubs(CardOperation.GET_RESOURCE) {
                    cardStubSuccess(CardOperation.GET_RESOURCE) {
                        this.responseResourceEntity = stubAudioResource
                    }
                    stubError(CardOperation.GET_RESOURCE)
                    stubError(CardOperation.GET_RESOURCE, AppStub.ERROR_AUDIO_RESOURCE_WRONG_RESOURCE_ID)
                    stubError(CardOperation.GET_RESOURCE, AppStub.ERROR_AUDIO_RESOURCE_NOT_FOUND)
                    stubError(CardOperation.GET_RESOURCE, AppStub.ERROR_AUDIO_RESOURCE_SERVER_ERROR)
                }
                normalizers(CardOperation.GET_RESOURCE)
                validators(CardOperation.GET_RESOURCE) {
                    validateUserId(CardOperation.GET_RESOURCE)
                    validateResourceGetLangId()
                    validateResourceGetWord()
                }
                runs(CardOperation.GET_RESOURCE) {
                    processFindUser(CardOperation.GET_RESOURCE)
                    processResource()
                }
            }

            operation(CardOperation.SEARCH_CARDS) {
                stubs(CardOperation.SEARCH_CARDS) {
                    cardStubSuccess(CardOperation.SEARCH_CARDS) {
                        this.responseCardEntityList = stubCards
                    }
                    stubError(CardOperation.SEARCH_CARDS)
                    stubError(CardOperation.SEARCH_CARDS, AppStub.ERROR_CARDS_WRONG_FILTER_LENGTH)
                    stubError(CardOperation.SEARCH_CARDS, AppStub.ERROR_WRONG_DICTIONARY_ID)
                }
                normalizers(CardOperation.SEARCH_CARDS)
                validators(CardOperation.SEARCH_CARDS) {
                    validateUserId(CardOperation.SEARCH_CARDS)
                    validateCardFilterLength { it.normalizedRequestCardFilter }
                    validateCardFilterDictionaryIds { it.normalizedRequestCardFilter }
                }
                runs(CardOperation.SEARCH_CARDS) {
                    processFindUser(CardOperation.SEARCH_CARDS)
                    processCardSearch()
                }
            }

            operation(CardOperation.GET_ALL_CARDS) {
                stubs(CardOperation.GET_ALL_CARDS) {
                    cardStubSuccess(CardOperation.GET_ALL_CARDS) {
                        this.responseCardEntityList = stubCards
                    }
                    stubError(CardOperation.GET_ALL_CARDS)
                    stubError(CardOperation.GET_ALL_CARDS, AppStub.ERROR_WRONG_DICTIONARY_ID)
                }
                normalizers(CardOperation.GET_ALL_CARDS)
                validators(CardOperation.GET_ALL_CARDS) {
                    validateUserId(CardOperation.GET_ALL_CARDS)
                    validateDictionaryId { (it as CardContext).normalizedRequestDictionaryId }
                }
                runs(CardOperation.GET_ALL_CARDS) {
                    processFindUser(CardOperation.GET_ALL_CARDS)
                    processGetAllCards()
                }
            }

            operation(CardOperation.CREATE_CARD) {
                stubs(CardOperation.CREATE_CARD) {
                    cardStubSuccess(CardOperation.CREATE_CARD) {
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
                normalizers(CardOperation.CREATE_CARD)
                validators(CardOperation.CREATE_CARD) {
                    validateUserId(CardOperation.CREATE_CARD)
                    validateCardEntityHasNoCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
                runs(CardOperation.CREATE_CARD) {
                    processFindUser(CardOperation.CREATE_CARD)
                    processCreateCard()
                }
            }

            operation(CardOperation.UPDATE_CARD) {
                stubs(CardOperation.UPDATE_CARD) {
                    cardStubSuccess(CardOperation.UPDATE_CARD) {
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
                normalizers(CardOperation.UPDATE_CARD)
                validators(CardOperation.UPDATE_CARD) {
                    validateUserId(CardOperation.UPDATE_CARD)
                    validateCardEntityHasValidCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
                runs(CardOperation.UPDATE_CARD) {
                    processFindUser(CardOperation.UPDATE_CARD)
                    processUpdateCard()
                }
            }

            operation(CardOperation.LEARN_CARDS) {
                stubs(CardOperation.LEARN_CARDS) {
                    cardStubSuccess(CardOperation.LEARN_CARDS)
                    stubError(CardOperation.LEARN_CARDS)
                    stubError(CardOperation.LEARN_CARDS, AppStub.ERROR_LEARN_CARD_WRONG_CARD_ID)
                    stubError(CardOperation.LEARN_CARDS, AppStub.ERROR_LEARN_CARD_WRONG_STAGES)
                    stubError(CardOperation.LEARN_CARDS, AppStub.ERROR_LEARN_CARD_WRONG_DETAILS)
                }
                normalizers(CardOperation.LEARN_CARDS)
                validators(CardOperation.LEARN_CARDS) {
                    validateUserId(CardOperation.LEARN_CARDS)
                    validateCardLearnListCardIds { it.normalizedRequestCardLearnList }
                    validateCardLearnListStages { it.normalizedRequestCardLearnList }
                    validateCardLearnListDetails { it.normalizedRequestCardLearnList }
                }
                runs(CardOperation.LEARN_CARDS) {
                    processFindUser(CardOperation.LEARN_CARDS)
                    processLearnCards()
                }
            }

            operation(CardOperation.GET_CARD) {
                stubs(CardOperation.GET_CARD) {
                    cardStubSuccess(CardOperation.GET_CARD) {
                        this.responseCardEntity = stubCard
                    }
                    stubError(CardOperation.GET_CARD)
                    stubError(CardOperation.GET_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                normalizers(CardOperation.GET_CARD)
                validators(CardOperation.GET_CARD) {
                    validateUserId(CardOperation.GET_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.GET_CARD) {
                    processFindUser(CardOperation.GET_CARD)
                    processGetCard()
                }
            }

            operation(CardOperation.RESET_CARD) {
                stubs(CardOperation.RESET_CARD) {
                    cardStubSuccess(CardOperation.RESET_CARD)
                    stubError(CardOperation.RESET_CARD)
                    stubError(CardOperation.RESET_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                normalizers(CardOperation.RESET_CARD)
                validators(CardOperation.RESET_CARD) {
                    validateUserId(CardOperation.RESET_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.RESET_CARD) {
                    processFindUser(CardOperation.RESET_CARD)
                    processResetCards()
                }
            }

            operation(CardOperation.DELETE_CARD) {
                stubs(CardOperation.DELETE_CARD) {
                    cardStubSuccess(CardOperation.DELETE_CARD)
                    stubError(CardOperation.DELETE_CARD)
                    stubError(CardOperation.DELETE_CARD, AppStub.ERROR_WRONG_CARD_ID)
                }
                normalizers(CardOperation.DELETE_CARD)
                validators(CardOperation.DELETE_CARD) {
                    validateUserId(CardOperation.DELETE_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.DELETE_CARD) {
                    processFindUser(CardOperation.DELETE_CARD)
                    processDeleteCard()
                }
            }
        }.build()
    }
}