package com.gitlab.sszuev.flashcards.speaker

abstract class BaseResourceException(id: String, message: String) : RuntimeException("[$id]::$message")