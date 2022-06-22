package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.Exec
import com.gitlab.sszuev.flashcards.corlib.ExecDSL

abstract class BaseChainDSLImpl<D>(
    override var name: String = "",
    override var description: String = "",
    protected val workers: MutableList<ExecDSL<D>> = mutableListOf(),
    protected var filter: suspend D.() -> Boolean = { true },
    protected var exceptionHandler: suspend D.(ex: Throwable) -> Unit = { ex: Throwable -> throw ex },
) : ChainDSL<D> {

    abstract override fun build(): Exec<D>

    override fun add(worker: ExecDSL<D>) {
        this.workers.add(worker)
    }

    override fun test(filter: suspend D.() -> Boolean) {
        this.filter = filter
    }

    override fun onException(exceptionHandler: suspend D.(ex: Throwable) -> Unit) {
        this.exceptionHandler = exceptionHandler
    }
}
