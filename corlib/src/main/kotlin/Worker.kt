package com.gitlab.sszuev.flashcards.corlib

/**
 * Represents a Worker.
 * @see <a href="https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern">Wiki: CoR</a>
 */
interface Worker<X> : Exec<X> {
    val title: String
    val description: String

    /**
     * Tests if the context suits to run the operation encapsulated by this worker.
     * @param [context]
     * @return [Boolean]
     */
    suspend fun test(context: X): Boolean

    /**
     * Handles an exception if it occurs while the operation.
     * @param [context]
     * @param [ex][Throwable]
     */
    suspend fun onException(context: X, ex: Throwable)

    /**
     * Performs the operation encapsulated by this worker against the given context.
     * @param [context]
     */
    suspend fun process(context: X)

    override suspend fun exec(context: X) {
        if (test(context)) {
            try {
                process(context)
            } catch (ex: Throwable) {
                onException(context, ex)
            }
        }
    }
}