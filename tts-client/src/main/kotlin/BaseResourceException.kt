package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.ResourceId

abstract class BaseResourceException(id: ResourceId, message: String) :
    RuntimeException("[${id.asString()}]::${message}")