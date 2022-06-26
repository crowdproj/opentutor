package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

internal fun ChainDSL<CardContext>.initContext() = worker {
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

internal fun ChainDSL<CardContext>.operation(
    operation: CardOperation,
    configure: ChainDSL<CardContext>.() -> Unit,
) = chain {
    this.name = "${operation.title().uppercase()} ::: operation"
    test {
        this.operation == operation && status == AppStatus.RUN
    }
    configure()
}

internal fun ChainDSL<CardContext>.stubs(
    operation: CardOperation,
    configure: ChainDSL<CardContext>.() -> Unit
) = chain {
    this.name = "${operation.title()} ::: stubs"
    test {
        this.workMode == AppMode.STUB && this.status == AppStatus.RUN
    }
    configure()
}

internal fun ChainDSL<CardContext>.validators(
    operation: CardOperation,
    configure: ChainDSL<CardContext>.() -> Unit
) = chain {
    this.name = "${operation.title()} ::: validation"
    test {
        this.workMode != AppMode.STUB && this.status == AppStatus.RUN
    }
    configure()
}

internal fun CardOperation.title(): String {
    return this.name.lowercase().replace("_", "-")
}