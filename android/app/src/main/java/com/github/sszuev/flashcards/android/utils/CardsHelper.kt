package com.github.sszuev.flashcards.android.utils

fun examplesAsString(examples: List<String>) = examples.joinToString("\n")

fun examplesAsList(examples: String) = examples.split("\n")

fun audioResource(lang: String, word: String): String = lang + ":" + word.replace(" ", "")

fun translationsAsString(translations: List<List<String>>) =
    translations.flatten().joinToString(", ")
