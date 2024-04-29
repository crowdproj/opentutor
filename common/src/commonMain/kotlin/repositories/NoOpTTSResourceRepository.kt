package com.gitlab.sszuev.flashcards.repositories

object NoOpTTSResourceRepository : TTSResourceRepository {
    override suspend fun findResource(lang: String, word: String): ByteArray? {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}