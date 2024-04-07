package com.gitlab.sszuev.flashcards.repositories

data class DbLang(
    val langId: String,
    val partsOfSpeech: List<String> = emptyList(),
) {
    companion object {
        val NULL = DbLang(langId = "", partsOfSpeech = emptyList())
    }
}