package com.gitlab.sszuev.flashcards.corlib

import com.gitlab.sszuev.flashcards.corlib.impl.ChainDSLImpl
import com.gitlab.sszuev.flashcards.corlib.impl.WorkerDSLImpl

/**
 * Creates a [Chain]-builder.
 * @param [configurer] a function to configure fresh instance of [Chain]-builder
 * @param [X] anything, context
 * @return a [ChainDSL] instance
 */
fun <X> chain(configurer: ChainDSL<X>.() -> Unit): ChainDSL<X> = ChainDSLImpl<X>().apply(configurer)

/**
 * Creates [Chain]-builder and add it to this [Chain]-builder.
 * @param [configurer] a function to configure fresh instance of [Chain]-builder
 */
@CorDSL
fun <X> ChainDSL<X>.chain(configurer: ChainDSL<X>.() -> Unit) {
    add(ChainDSLImpl<X>().apply(configurer))
}

/**
 * Creates [Worker]-builder and add it to this [Chain]-builder.
 * @param [configurer] a function to configure fresh instance of [Worker]-builder
 */
@CorDSL
fun <X> ChainDSL<X>.worker(configurer: WorkerDSL<X>.() -> Unit) {
    add(WorkerDSLImpl<X>().apply(configurer))
}

/**
 * Creates [Worker]-builder and add it to this [Chain]-builder.
 * @param [name] (title) of the future [Worker]
 * @param [description] of the future [Worker]
 * @param [runnable] a function that will be called on [Worker.process] execution
 */
@CorDSL
fun <X> ChainDSL<X>.worker(
    name: String,
    description: String = "",
    runnable: suspend X.() -> Unit
) {
    add(WorkerDSLImpl<X>().apply {
        this.name = name
        this.description = description
        this.process(runnable)
    })
}



