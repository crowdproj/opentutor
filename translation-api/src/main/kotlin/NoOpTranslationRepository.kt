package com.gitlab.sszuev.flashcards.translation.api

object NoOpTranslationRepository : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): TCard = noOp()

    private fun noOp(): Nothing = error("Must not be called.")
}