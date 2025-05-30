package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.NONE
import kotlinx.datetime.Instant

data class DbCard(
    val cardId: String,
    val dictionaryId: String,
    val words: List<Word>,
    val stats: Map<String, Long>,
    val details: Map<String, Any>,
    val answered: Int?,
    val changedAt: Instant,
) {
    data class Word(
        val word: String,
        val transcription: String?,
        val partOfSpeech: String?,
        val examples: List<Example>,
        val translations: List<List<String>>,
        val primary: Boolean,
    ) {
        data class Example(
            val text: String,
            val translation: String?,
        ) {
            companion object {
                val NULL = Example(
                    text = "",
                    translation = null,
                )
            }
        }

        companion object {
            val NULL = Word(
                word = "",
                transcription = null,
                partOfSpeech = null,
                examples = emptyList(),
                translations = emptyList(),
                primary = false,
            )
        }
    }

    companion object {
        val NULL = DbCard(
            cardId = "",
            dictionaryId = "",
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = emptyList(),
        )
    }
}
