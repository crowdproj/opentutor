package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.TTSContext

interface TTSService {
    /**
     * Gets a card-resource (byte array).
     */
    suspend fun getResource(context: TTSContext): TTSContext
}