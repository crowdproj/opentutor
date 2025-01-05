package com.github.sszuev.flashcards.android

import com.github.sszuev.flashcards.android.repositories.CardResource
import com.github.sszuev.flashcards.android.repositories.DictionaryResource

fun DictionaryResource.toDictionary() = Dictionary(
    dictionaryId = this.dictionaryId,
    name = checkNotNull(this.name),
    sourceLanguage = checkNotNull(this.sourceLang),
    targetLanguage = checkNotNull(this.targetLang),
    totalWords = checkNotNull(this.total),
    learnedWords = checkNotNull(this.learned),
    numberOfRightAnswers = this.numberOfRightAnswers ?: 15
)

fun CardResource.toCard() = Card(
    dictionaryId = checkNotNull(this.dictionaryId),
    cardId = checkNotNull(this.cardId),
    word = checkNotNull(this.words) { "No words" }
        .map { checkNotNull(it.word) { "No word" } }
        .firstOrNull() ?: throw IllegalArgumentException("Can't find field 'word' for card = $cardId"),
    translation = checkNotNull(this.words).firstNotNullOfOrNull {
        checkNotNull(it.translations) { "No translation" }.flatten().firstOrNull()
    } ?: throw IllegalArgumentException("Can't find field 'translation' for card = $cardId"),
    answered = checkNotNull(answered),
)

fun Dictionary.toDictionaryResource() = DictionaryResource(
    dictionaryId = this.dictionaryId,
    name = this.name,
    sourceLang = this.sourceLanguage,
    targetLang = this.targetLanguage,
    total= this.totalWords,
    learned = this.learnedWords,
    numberOfRightAnswers = this.numberOfRightAnswers,
)