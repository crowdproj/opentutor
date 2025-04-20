package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.processCardSearch
import com.gitlab.sszuev.flashcards.core.processes.processCreateCard
import com.gitlab.sszuev.flashcards.core.processes.processDeleteCard
import com.gitlab.sszuev.flashcards.core.processes.processGetAllCards
import com.gitlab.sszuev.flashcards.core.processes.processGetCard
import com.gitlab.sszuev.flashcards.core.processes.processLearnCards
import com.gitlab.sszuev.flashcards.core.processes.processResetCard
import com.gitlab.sszuev.flashcards.core.processes.processResetCards
import com.gitlab.sszuev.flashcards.core.processes.processUpdateCard
import com.gitlab.sszuev.flashcards.core.validators.validateCardEntityDictionaryId
import com.gitlab.sszuev.flashcards.core.validators.validateCardEntityHasNoCardId
import com.gitlab.sszuev.flashcards.core.validators.validateCardEntityHasValidCardId
import com.gitlab.sszuev.flashcards.core.validators.validateCardEntityWords
import com.gitlab.sszuev.flashcards.core.validators.validateCardFilterDictionaryIds
import com.gitlab.sszuev.flashcards.core.validators.validateCardFilterLength
import com.gitlab.sszuev.flashcards.core.validators.validateCardId
import com.gitlab.sszuev.flashcards.core.validators.validateCardLearnListCardIds
import com.gitlab.sszuev.flashcards.core.validators.validateCardLearnListDetails
import com.gitlab.sszuev.flashcards.core.validators.validateCardLearnListStages
import com.gitlab.sszuev.flashcards.core.validators.validateDictionaryId
import com.gitlab.sszuev.flashcards.core.validators.validateUserId
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

/**
 * Main class fot business logic,
 * it is based on the Chain-Of-Responsibility (COR) pattern.
 */
class CardCorProcessor {

    suspend fun execute(context: CardContext) = businessChain.exec(context)

    companion object {
        private val businessChain = chain {
            name = "CardContext Root Chain"
            initContext()

            operation(CardOperation.SEARCH_CARDS) {
                normalizers(CardOperation.SEARCH_CARDS)
                validators(CardOperation.SEARCH_CARDS) {
                    validateUserId(CardOperation.SEARCH_CARDS)
                    validateCardFilterLength { it.normalizedRequestCardFilter }
                    validateCardFilterDictionaryIds { it.normalizedRequestCardFilter }
                }
                runs(CardOperation.SEARCH_CARDS) {
                    processCardSearch()
                }
            }

            operation(CardOperation.GET_ALL_CARDS) {
                normalizers(CardOperation.GET_ALL_CARDS)
                validators(CardOperation.GET_ALL_CARDS) {
                    validateUserId(CardOperation.GET_ALL_CARDS)
                    validateDictionaryId { it.normalizedRequestDictionaryId }
                }
                runs(CardOperation.GET_ALL_CARDS) {
                    processGetAllCards()
                }
            }

            operation(CardOperation.CREATE_CARD) {
                normalizers(CardOperation.CREATE_CARD)
                validators(CardOperation.CREATE_CARD) {
                    validateUserId(CardOperation.CREATE_CARD)
                    validateCardEntityHasNoCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWords { it.normalizedRequestCardEntity }
                }
                runs(CardOperation.CREATE_CARD) {
                    processCreateCard()
                }
            }

            operation(CardOperation.UPDATE_CARD) {
                normalizers(CardOperation.UPDATE_CARD)
                validators(CardOperation.UPDATE_CARD) {
                    validateUserId(CardOperation.UPDATE_CARD)
                    validateCardEntityHasValidCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWords { it.normalizedRequestCardEntity }
                }
                runs(CardOperation.UPDATE_CARD) {
                    processUpdateCard()
                }
            }

            operation(CardOperation.LEARN_CARDS) {
                normalizers(CardOperation.LEARN_CARDS)
                validators(CardOperation.LEARN_CARDS) {
                    validateUserId(CardOperation.LEARN_CARDS)
                    validateCardLearnListCardIds { it.normalizedRequestCardLearnList }
                    validateCardLearnListStages { it.normalizedRequestCardLearnList }
                    validateCardLearnListDetails { it.normalizedRequestCardLearnList }
                }
                runs(CardOperation.LEARN_CARDS) {
                    processLearnCards()
                }
            }

            operation(CardOperation.RESET_CARDS) {
                normalizers(CardOperation.RESET_CARDS)
                validators(CardOperation.RESET_CARDS) {
                    validateUserId(CardOperation.RESET_CARDS)
                    validateDictionaryId { it.normalizedRequestDictionaryId }
                }
                runs(CardOperation.RESET_CARDS) {
                    processResetCards()
                }
            }

            operation(CardOperation.GET_CARD) {
                normalizers(CardOperation.GET_CARD)
                validators(CardOperation.GET_CARD) {
                    validateUserId(CardOperation.GET_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.GET_CARD) {
                    processGetCard()
                }
            }

            operation(CardOperation.RESET_CARD) {
                normalizers(CardOperation.RESET_CARD)
                validators(CardOperation.RESET_CARD) {
                    validateUserId(CardOperation.RESET_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.RESET_CARD) {
                    processResetCard()
                }
            }

            operation(CardOperation.DELETE_CARD) {
                normalizers(CardOperation.DELETE_CARD)
                validators(CardOperation.DELETE_CARD) {
                    validateUserId(CardOperation.DELETE_CARD)
                    validateCardId { it.normalizedRequestCardEntityId }
                }
                runs(CardOperation.DELETE_CARD) {
                    processDeleteCard()
                }
            }
        }.build()
    }
}