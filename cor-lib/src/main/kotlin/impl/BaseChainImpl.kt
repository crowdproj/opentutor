package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.Chain

abstract class BaseChainImpl<X>(
    override val title: String,
    override val description: String = "",
    private val filter: suspend X.() -> Boolean = { true },
    private val exceptionHandler: suspend X.(Throwable) -> Unit = {},
) : Chain<X> {

    override suspend fun test(context: X): Boolean = filter(context)

    override suspend fun onException(context: X, ex: Throwable) = exceptionHandler(context, ex)

    abstract override suspend fun process(context: X)

    override fun toString(): String {
        return "Chain(title='$title', description='$description')"
    }
}