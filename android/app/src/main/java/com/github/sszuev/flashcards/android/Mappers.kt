package com.github.sszuev.flashcards.android

import com.github.sszuev.flashcards.android.repositories.DictionaryResource

fun DictionaryResource.toDictionary() = Dictionary(
    name = checkNotNull(this.name),
    sourceLanguage = checkNotNull(this.sourceLang),
    targetLanguage = checkNotNull(this.targetLang),
    totalWords = checkNotNull(this.total),
    learnedWords = checkNotNull(this.learned),
)