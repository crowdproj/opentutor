package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.stubs.createCardFailUnknownStub
import com.gitlab.sszuev.flashcards.core.stubs.createCardSuccessStub
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
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
                createCardSuccessStub()
                createCardFailUnknownStub()
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