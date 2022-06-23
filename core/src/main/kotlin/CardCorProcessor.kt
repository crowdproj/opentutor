package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.stubs.createCardErrorStub
import com.gitlab.sszuev.flashcards.core.stubs.createCardSuccessStub
import com.gitlab.sszuev.flashcards.core.stubs.createCardUnknownErrorStub
import com.gitlab.sszuev.flashcards.core.validation.normalize
import com.gitlab.sszuev.flashcards.core.validation.validateCardId
import com.gitlab.sszuev.flashcards.core.validation.validateCardWord
import com.gitlab.sszuev.flashcards.core.validation.validateDictionaryId
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppStatus
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

            operation("Create card", CardOperation.CREATE_CARD) {
                chain {
                    name = "Handle stubs"
                    test {
                        this.workMode == AppMode.STUB && this.status == AppStatus.RUN
                    }
                    createCardSuccessStub()
                    createCardUnknownErrorStub()
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_WORD)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_TRANSLATION)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_TRANSCRIPTION)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_EXAMPLES)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_DETAILS)
                    createCardErrorStub(AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE)
                }
                chain {
                    name = "Card validation"
                    worker(name = "Make a normalized copy of request") {
                        this.normalizedRequestCardEntity = this.requestCardEntity.normalize()
                    }
                    validateCardId { it.normalizedRequestCardEntity.cardId }
                    validateDictionaryId { it.normalizedRequestCardEntity.dictionaryId }
                    validateCardWord { it.normalizedRequestCardEntity }
                }
            }
        }

        private fun ChainDSL<CardContext>.initContext() = worker {
            worker {
                this.name = "start context"
                this.description = "prepare generic fields"
                test {
                    this.status == AppStatus.INIT
                }
                process {
                    this.status = AppStatus.RUN
                }
            }
        }

        private fun ChainDSL<CardContext>.operation(
            name: String,
            operation: CardOperation,
            configure: ChainDSL<CardContext>.() -> Unit,
        ) = chain {
            configure()
            this.name = name
            test {
                this.operation == operation && status == AppStatus.RUN
            }
        }
    }
}