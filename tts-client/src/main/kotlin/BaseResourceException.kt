package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

abstract class BaseResourceException(id: TTSResourceId, message: String) :
    RuntimeException("[${id.asString()}]::${message}")