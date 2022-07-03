package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.CorDSL
import com.gitlab.sszuev.flashcards.corlib.Exec

@CorDSL
class ChainDSLImpl<D> : BaseChainDSLImpl<D>() {
    override fun build(): Exec<D> = ChainImpl(
        title = name,
        description = description,
        executors = workers.map { it.build() }.toList(),
        filter = filter,
        exceptionHandler = exceptionHandler,
    )
}