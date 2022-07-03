package com.gitlab.sszuev.flashcards.corlib

/**
 * Base executor.
 */
interface Exec<X> {
    /**
     * Performs the operation encapsulated by this exec against the context.
     * @param [context]
     */
    suspend fun exec(context: X)
}