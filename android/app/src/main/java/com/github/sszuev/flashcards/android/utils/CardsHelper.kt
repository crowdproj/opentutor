package com.github.sszuev.flashcards.android.utils

import com.github.sszuev.flashcards.android.CELL_TEXT_LIMIT
import com.github.sszuev.flashcards.android.STAGE_WRITING_PREFIX_LENGTH
import com.github.sszuev.flashcards.android.entities.CardEntity
import kotlin.math.max

fun examplesAsString(examples: List<Pair<String, String?>>): String = examples.joinToString("\n") {
    exampleAsString(it)
}

fun exampleAsString(example: Pair<String, String?>) = if (example.second != null)
    example.first + " — " + example.second
else
    example.first

fun examplesAsList(examples: String): List<Pair<String, String?>> {
    if (examples.isEmpty()) {
        return emptyList()
    }
    return examples.split("\n").map {
        val parts = it.split(" — ")
        if (parts.size == 1) {
            parts[0] to null
        } else {
            parts[0] to parts[1]
        }
    }
}

fun audioResource(lang: String, word: String): String = lang + ":" + word.replace(" ", "")

fun wordFromAudioResource(audioResourceId: String): String {
    return audioResourceId.substringAfter(":")
}

fun langFromAudioResource(audioResourceId: String): String {
    return audioResourceId.substringBefore(":")
}

fun isTextShort(text: String) = text.length <= CELL_TEXT_LIMIT

fun shortText(text: String) =
    if (text.length > CELL_TEXT_LIMIT) text.take(CELL_TEXT_LIMIT - 3) + "..." else text

fun translationFromString(translation: String) = translation.split(",").map { it.trim() }

fun wordAsList(word: String) = listOf(word.trim())

fun normalizeWord(word: String) = word.replace("\n", "").trim()

fun correctAnswerIndexOf(samples: List<String>, input: String): Int {
    val txt = input.normalize()
    if (txt.length < STAGE_WRITING_PREFIX_LENGTH) {
        samples.forEachIndexed { index, tr ->
            if (tr.normalize() == txt) {
                return index
            }
        }
        return -1
    }
    samples.forEachIndexed { index, tr ->
        if (tr.normalize()
                .startsWith(txt.take(max(txt.length, STAGE_WRITING_PREFIX_LENGTH)))
        ) {
            return index
        }
    }
    return -1
}

private fun String.normalize() = trim().lowercase().replace("ё", "е")

val CardEntity.translationAsString
    get() = translation.joinToString(", ")