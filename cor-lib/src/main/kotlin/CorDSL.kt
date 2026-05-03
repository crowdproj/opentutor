package com.gitlab.sszuev.flashcards.corlib

@DslMarker
annotation class CorDSL

@CorDSL
interface ChainDSL<D> : ExecDSL<D>, HandlerDSL<D> {
    fun add(worker: ExecDSL<D>)
}

@CorDSL
interface WorkerDSL<D> : ExecDSL<D>, HandlerDSL<D> {
    fun process(runnable: suspend D.() -> Unit)
}

interface ExecDSL<D> {
    var name: String

    var description: String

    fun build(): Exec<D>
}

interface HandlerDSL<D> {
    fun test(filter: suspend D.() -> Boolean)

    fun onException(exceptionHandler: suspend D.(ex: Throwable) -> Unit)
}
