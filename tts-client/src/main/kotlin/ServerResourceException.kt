package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.ResourceId

class ServerResourceException(id: ResourceId, message: String) : BaseResourceException(id, message)