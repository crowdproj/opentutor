package com.gitlab.sszuev.flashcards.repositories

class DbDataException(override val message: String, override val cause: Throwable? = null) : Exception(message, cause)