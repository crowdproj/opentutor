package com.gitlab.sszuev.flashcards.corlib

/**
 * Describes a chain-worker: sequence of other [Worker]s.
 */
interface Chain<X> : Worker<X>