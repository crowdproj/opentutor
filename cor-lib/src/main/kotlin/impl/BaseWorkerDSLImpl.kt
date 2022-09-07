package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.Exec
import com.gitlab.sszuev.flashcards.corlib.WorkerDSL

abstract class BaseWorkerDSLImpl<D>(
    override var name: String = "",
    override var description: String = "",
    protected var filter: suspend D.() -> Boolean = { true },
    protected var runnable: suspend D.() -> Unit = {},
    protected var exceptionHandler: suspend D.(ex: Throwable) -> Unit = { ex: Throwable -> throw ex },
) : WorkerDSL<D> {

    abstract override fun build(): Exec<D>

    override fun test(filter: suspend D.() -> Boolean) {
        this.filter = filter
    }

    override fun process(runnable: suspend D.() -> Unit) {
        this.runnable = runnable
    }

    override fun onException(exceptionHandler: suspend D.(ex: Throwable) -> Unit) {
        this.exceptionHandler = exceptionHandler
    }
}