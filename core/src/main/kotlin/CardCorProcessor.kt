package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.stubs.*
import com.gitlab.sszuev.flashcards.core.validation.*
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

/**
 * Main class fot business logic,
 * it is based on Chain-Of-Responsibility (COR) pattern.
 */
class CardCorProcessor {

    suspend fun execute(context: CardContext) = businessChain.build().exec(context)

    companion object {
        private val businessChain = chain {
            initContext()

            operation("Search cards", CardOperation.SEARCH_CARDS) {
                stubs("search-cards :: handle stubs") {
                    searchCardsSuccessStub()
                    unknownErrorStub("Stub :: search-cards fail unknown")
                    searchCardsErrorStub(AppStub.ERROR_CARDS_FILTER_WRONG_LENGTH)
                    searchCardsErrorStub(AppStub.ERROR_CARDS_FILTER_WRONG_DICTIONARY_ID)
                }
                validators("search-cards :: validation") {
                    worker(name = "Make a normalized copy of search-cards request") {
                        this.normalizedRequestCardFilter = this.requestCardFilter.normalize()
                    }
                    validateCardFilterLength { it.normalizedRequestCardFilter }
                    validateCardFilterDictionaryIds { it.normalizedRequestCardFilter }
                }
            }

            operation("Create card", CardOperation.CREATE_CARD) {
                stubs("create-card :: handle stubs") {
                    createCardSuccessStub()
                    unknownErrorStub("Stub :: create-card fail unknown")
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_WORD)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_TRANSLATION)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_TRANSCRIPTION)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_EXAMPLES)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_DETAILS)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE)
                }
                validators("create-card :: validation") {
                    worker(name = "Make a normalized copy of get-card request") {
                        this.normalizedRequestCardEntity = this.requestCardEntity.normalize()
                    }
                    validateCardEntityCardId { it.normalizedRequestCardEntity }
                    validateCardEntityDictionaryId { it.normalizedRequestCardEntity }
                    validateCardEntityWord { it.normalizedRequestCardEntity }
                }
            }

            operation("Learn card", CardOperation.LEARN_CARD) {
                stubs("learn-card :: handle stubs") {
                    learnCardSuccessStub()
                    unknownErrorStub("Stub :: learn-card fail unknown")
                    createCardErrorStub(AppStub.ERROR_LEARN_CARD_WRONG_CARD_ID)
                    createCardErrorStub(AppStub.ERROR_LEARN_CARD_WRONG_STAGES)
                    createCardErrorStub(AppStub.ERROR_LEARN_CARD_WRONG_DETAILS)
                }
                validators("learn-card :: validation") {
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