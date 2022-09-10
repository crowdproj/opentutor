package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.Worker

class WorkerImpl<X>(
    override val title: String,
    override val description: String = "",
    private val filter: suspend X.() -> Boolean = { true },
    private val handler: suspend X.() -> Unit = {},
    private val exceptionHandler: suspend X.(Throwable) -> Unit = {},
) : Worker<X> {

    override suspend fun test(context: X): Boolean = filter(context)

    override suspend fun process(context: X) = handler(context)

    override suspend fun onException(context: X, ex: Throwable) = exceptionHandler(context, ex)

    override fun toString(): String {
        return "Worker(title='$title', description='$description')"
    }
}