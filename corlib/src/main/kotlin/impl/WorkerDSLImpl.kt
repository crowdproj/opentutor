package com.gitlab.sszuev.flashcards.corlib.impl

import com.gitlab.sszuev.flashcards.corlib.CorDSL
import com.gitlab.sszuev.flashcards.corlib.Exec

@CorDSL
class WorkerDSLImpl<D> : BaseWorkerDSLImpl<D>() {

    override fun build(): Exec<D> = WorkerImpl(
        title = name,
        description = description,
        filter = filter,
        handler = runnable,
        exceptionHandler = exceptionHandler,
    )
}