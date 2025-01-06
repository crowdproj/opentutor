package com.github.sszuev.flashcards.android

import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.entities.DictionaryEntity
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.repositories.CardResource
import com.github.sszuev.flashcards.android.repositories.CardWordExampleResource
import com.github.sszuev.flashcards.android.repositories.CardWordResource
import com.github.sszuev.flashcards.android.repositories.DictionaryResource
import com.github.sszuev.flashcards.android.repositories.SettingsResource

fun DictionaryResource.toDictionary() = DictionaryEntity(
    dictionaryId = this.dictionaryId,
    name = checkNotNull(this.name),
    sourceLanguage = checkNotNull(this.sourceLang),
    targetLanguage = checkNotNull(this.targetLang),
    totalWords = checkNotNull(this.total),
    learnedWords = checkNotNull(this.learned),
    numberOfRightAnswers = this.numberOfRightAnswers ?: 15
)

fun CardResource.toCard(): CardEntity {
    val w = checkNotNull(this.words) { "No words" }
        .firstOrNull()
        ?: throw IllegalArgumentException("Can't find field 'word' for card = $cardId")
    return CardEntity(
        dictionaryId = checkNotNull(this.dictionaryId),
        cardId = checkNotNull(this.cardId),
        word = checkNotNull(w.word) { "No word for $cardId" },
        translation = checkNotNull(this.words).firstNotNullOfOrNull {
            checkNotNull(it.translations) { "No translation" }.flatten().firstOrNull()
        } ?: throw IllegalArgumentException("Can't find field 'translation' for card = $cardId"),
        answered = answered ?: 0,
        examples = w.examples?.mapNotNull { it.example } ?: emptyList(),
        audioId = checkNotNull(w.sound),
    )
}

fun CardEntity.toCardResource(): CardResource {
    val examples = this.examples.map {
        CardWordExampleResource(
            example = it
        )
    }
    val word = CardWordResource(
        word = word,
        translations = listOf(listOf(translation)),
        examples = examples,
        sound = this.audioId,
    )
    return CardResource(
        dictionaryId = this.dictionaryId,
        cardId = this.cardId,
        words = listOf(word),
        answered = answered,
    )
}


fun DictionaryEntity.toDictionaryResource() = DictionaryResource(
    dictionaryId = this.dictionaryId,
    name = this.name,
    sourceLang = this.sourceLanguage,
    targetLang = this.targetLanguage,
    total = this.totalWords,
    learned = this.learnedWords,
    numberOfRightAnswers = this.numberOfRightAnswers,
)

fun SettingsEntity.toSettingsResource() = SettingsResource(
    stageShowNumberOfWords = stageShowNumberOfWords,
    stageOptionsNumberOfVariants = stageOptionsNumberOfVariants,
    numberOfWordsPerStage = numberOfWordsPerStage,
    stageMosaicSourceLangToTargetLang = stageMosaicSourceLangToTargetLang,
    stageOptionsSourceLangToTargetLang = stageOptionsSourceLangToTargetLang,
    stageWritingSourceLangToTargetLang = stageWritingSourceLangToTargetLang,
    stageSelfTestSourceLangToTargetLang = stageSelfTestSourceLangToTargetLang,
    stageMosaicTargetLangToSourceLang = stageMosaicTargetLangToSourceLang,
    stageOptionsTargetLangToSourceLang = stageOptionsTargetLangToSourceLang,
    stageWritingTargetLangToSourceLang = stageWritingTargetLangToSourceLang,
    stageSelfTestTargetLangToSourceLang = stageSelfTestTargetLangToSourceLang,
)

fun SettingsResource.toSettings() = SettingsEntity(
    stageShowNumberOfWords = stageShowNumberOfWords ?: 6,
    stageOptionsNumberOfVariants = stageOptionsNumberOfVariants ?: 6,
    numberOfWordsPerStage = numberOfWordsPerStage ?: 15,
    stageMosaicSourceLangToTargetLang = stageMosaicSourceLangToTargetLang ?: true,
    stageOptionsSourceLangToTargetLang = stageOptionsSourceLangToTargetLang ?: true,
    stageWritingSourceLangToTargetLang = stageWritingSourceLangToTargetLang ?: true,
    stageSelfTestSourceLangToTargetLang = stageSelfTestSourceLangToTargetLang ?: true,
    stageMosaicTargetLangToSourceLang = stageMosaicTargetLangToSourceLang ?: false,
    stageOptionsTargetLangToSourceLang = stageOptionsTargetLangToSourceLang ?: false,
    stageWritingTargetLangToSourceLang = stageWritingTargetLangToSourceLang ?: false,
    stageSelfTestTargetLangToSourceLang = stageSelfTestTargetLangToSourceLang ?: false,
)