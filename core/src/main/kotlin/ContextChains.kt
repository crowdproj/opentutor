package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus

internal inline fun <reified Context : AppContext> ChainDSL<Context>.initContext() = worker {
    worker {
        this.name = "start context ${Context::class.java.simpleName}"
        this.description = "prepare generic fields for context ${Context::class.java.simpleName}"
        test {
            this.status == AppStatus.INIT
        }
        process {
            this.status = AppStatus.RUN
        }
    }
}

internal fun <Context : AppContext> ChainDSL<Context>.operation(
    operation: AppOperation,
    configure: ChainDSL<Context>.() -> Unit,
) = chain {
    this.name = "${operation.name} ::: operation"
    test {
        this.operation == operation && status == AppStatus.RUN
    }
    configure()
}

internal fun <Context : AppContext> ChainDSL<Context>.stubs(
    operation: AppOperation,
    configure: ChainDSL<Context>.() -> Unit
) = chain {
    this.name = "${operation.name} ::: stubs"
    test {
        this.operation == operation && this.workMode == AppMode.STUB && this.status == AppStatus.RUN
    }
    configure()
}

internal fun <Context : AppContext> ChainDSL<Context>.validators(
    operation: AppOperation,
    configure: ChainDSL<Context>.() -> Unit
) = chain {
    this.name = "${operation.name} ::: validation"
    test {
        this.operation == operation && this.status == AppStatus.RUN
    }
    configure()
}

internal fun <Context : AppContext> ChainDSL<Context>.runs(
    operation: AppOperation,
    configure: ChainDSL<Context>.() -> Unit
) = chain {
    this.name = "${operation.name} ::: process"
    test {
        this.operation == operation && this.workMode != AppMode.STUB && this.status == AppStatus.RUN
    }
    configure()
    finish(operation)
}

internal fun <Context : AppContext> ChainDSL<Context>.finish(
    operation: AppOperation,
) = worker {
    this.name = "${operation.name} ::: finish"
    test {
        this.operation == operation && this.workMode != AppMode.STUB && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.OK
    }
}