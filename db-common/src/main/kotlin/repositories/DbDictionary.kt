package com.gitlab.sszuev.flashcards.repositories

data class DbDictionary(
    val dictionaryId: String,
    val userId: String,
    val name: String,
    val sourceLang: DbLang,
    val targetLang: DbLang,
) {
    companion object {
        val NULL = DbDictionary(
            dictionaryId = "",
            userId = "",
            name = "",
            sourceLang = DbLang.NULL,
            targetLang = DbLang.NULL,
        )
    }
}