package com.github.sszuev.flashcards.android.utils

import com.github.sszuev.flashcards.android.CELL_TEXT_LIMIT
import com.github.sszuev.flashcards.android.STAGE_WRITING_PREFIX_LENGTH
import com.github.sszuev.flashcards.android.entities.CardEntity

fun examplesAsString(examples: List<String>) = examples.joinToString("\n")

fun examplesAsList(examples: String) = if (examples.isBlank()) emptyList() else examples.split("\n")

fun audioResource(lang: String, word: String): String = lang + ":" + word.replace(" ", "")

fun isTextShort(text: String) = text.length <= CELL_TEXT_LIMIT

fun shortText(text: String) =
    if (text.length > CELL_TEXT_LIMIT) text.substring(0, CELL_TEXT_LIMIT - 3) + "..." else text

fun translationFromString(translation: String) = translation.split(",").map { it.trim() }

fun wordAsList(word: String) = word.split(", ").map { it.trim() }

fun correctAnswerIndexOf(translation: List<String>, input: String): Int {
    val txt = input.trim()
    if (txt.length < STAGE_WRITING_PREFIX_LENGTH) {
        translation.forEachIndexed { index, tr ->
            if (tr.trim().equals(txt, true)) {
                return index
            }
        }
        return -1
    }
    translation.forEachIndexed { index, tr ->
        if (tr.trim().startsWith(txt.substring(0, STAGE_WRITING_PREFIX_LENGTH), true)) {
            return index
        }
    }
    return -1
}

val CardEntity.translationAsString
    get() = translation.joinToString(", ")