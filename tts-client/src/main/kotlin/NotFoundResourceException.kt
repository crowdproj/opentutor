package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

class NotFoundResourceException(id: TTSResourceId, message: String) : BaseResourceException(id, message)
