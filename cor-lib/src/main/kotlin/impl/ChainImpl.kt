package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.Exec

class ChainImpl<X>(
    private val executors: List<Exec<X>>,
    title: String,
    description: String = "",
    filter: suspend X.() -> Boolean = { true },
    exceptionHandler: suspend X.(ex: Throwable) -> Unit = {},
) : BaseChainImpl<X>(
    title = title,
    description = description,
    filter = filter,
    exceptionHandler = exceptionHandler,
) {
    override suspend fun process(context: X) {
        executors.forEach { it.exec(context) }
    }
}