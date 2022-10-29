package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

class ServerResourceException(id: TTSResourceId, message: String) : BaseResourceException(id, message)