package com.gitlab.sszuev.flashcards.services.local

import com.gitlab.sszuev.flashcards.services.HealthService

class LocalHealthService : HealthService {
    override fun ping(): Boolean = true
}