package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.AppConfig
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbLang

fun CardEntity.toDbCard() = DbCard(
    cardId = this.cardId.asString(),
    dictionaryId = this.dictionaryId.asString(),
    answered = this.answered,
    changedAt = this.changedAt,
    stats = this.stats.mapKeys { it.key.name },
    words = this.words.map { it.toDbCardWord() },
    details = this.details,
)

fun DbCard.toCardEntity() = CardEntity(
    cardId = CardId(this.cardId),
    dictionaryId = DictionaryId(this.dictionaryId),
    answered = this.answered,
    changedAt = this.changedAt,
    stats = this.stats.mapKeys { Stage.valueOf(it.key) },
    words = this.words.map { it.toCardWordEntity() },
    details = this.details,
)

fun DictionaryEntity.toDbDictionary() = DbDictionary(
    dictionaryId = this.dictionaryId.asString(),
    name = this.name,
    userId = this.userId.asString(),
    sourceLang = this.sourceLang.toDbLang(),
    targetLang = this.targetLang.toDbLang(),
    details = mapOf("numberOfRightAnswers" to this.numberOfRightAnswers),
)

fun DbDictionary.toDictionaryEntity(config: AppConfig) = DictionaryEntity(
    dictionaryId = DictionaryId(this.dictionaryId),
    name = this.name,
    userId = AppAuthId(this.userId),
    sourceLang = this.sourceLang.toLangEntity(),
    targetLang = this.targetLang.toLangEntity(),
    numberOfRightAnswers = details["numberOfRightAnswers"]?.toString()?.toInt() ?: config.numberOfRightAnswers,
)

private fun CardWordEntity.toDbCardWord() = DbCard.Word(
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toDbCardWordExample() },
    translations = this.translations,
)

private fun CardWordExampleEntity.toDbCardWordExample() =
    DbCard.Word.Example(text = this.text, translation = this.translation)

private fun DbCard.Word.toCardWordEntity() = CardWordEntity(
    word = this.word,
    transcription = this.transcription,
    partOfSpeech = this.partOfSpeech,
    examples = this.examples.map { it.toCardWordExampleEntity() },
    translations = this.translations,
)

private fun DbCard.Word.Example.toCardWordExampleEntity() =
    CardWordExampleEntity(text = this.text, translation = this.translation)

private fun DbLang.toLangEntity() = LangEntity(
    langId = LangId(langId),
    partsOfSpeech = partsOfSpeech,
)

private fun LangEntity.toDbLang() = DbLang(
    langId = langId.asString(),
    partsOfSpeech = partsOfSpeech,
)