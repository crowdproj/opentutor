@file:OptIn(ExperimentalTime::class)

package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.AppConfig
import com.gitlab.sszuev.flashcards.core.documents.DocumentCard
import com.gitlab.sszuev.flashcards.core.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.core.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.repositories.LanguageRepository
import kotlin.time.ExperimentalTime

fun DocumentDictionary.toDictionaryEntity(): DictionaryEntity = DictionaryEntity(
    name = this.name,
    sourceLang = createLangEntity(this.sourceLang),
    targetLang = createLangEntity(this.targetLang),
)

fun DictionaryEntity.toXmlDocumentDictionary(): DocumentDictionary = DocumentDictionary(
    name = this.name,
    sourceLang = this.sourceLang.langId.asString(),
    targetLang = this.targetLang.langId.asString(),
    cards = emptyList(),
)

fun DocumentCard.toCardEntity(config: AppConfig): CardEntity = CardEntity(
    words = this.toCardWordEntity(),
    details = emptyMap(),
    answered = config.answered(this.status),
)

fun CardEntity.toXmlDocumentCard(config: AppConfig): DocumentCard {
    val word = this.words.first()
    return DocumentCard(
        text = word.word,
        transcription = word.transcription,
        partOfSpeech = word.partOfSpeech,
        translations = word.toDocumentTranslations(),
        examples = word.toDocumentExamples(),
        status = config.status(this.answered),
    )
}

internal fun createLangEntity(documentTag: String): LangEntity = LangEntity(
    langId = LangId(documentTag),
    partsOfSpeech = LanguageRepository.partsOfSpeech(documentTag)
)

private fun CardWordEntity.toDocumentTranslations(): List<String> = translations.map { it.joinToString(",") }

private fun CardWordEntity.toDocumentExamples(): List<String> =
    examples.map { if (it.translation != null) "${it.text} -- ${it.translation}" else it.text }

private fun DocumentCard.toCardWordEntity(): List<CardWordEntity> {
    val forms = this.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val primaryTranslations = this.translations.map {
        fromDocumentCardTranslationToCommonWordDtoTranslation(it)
    }
    val primaryExamples = this.examples.map { example ->
        val parts = example.split(" -- ").filter { it.isNotEmpty() }
        val (e, t) = if (parts.size == 2) {
            parts[0] to parts[1]
        } else {
            example to null
        }
        CardWordExampleEntity(text = e, translation = t)
    }
    return forms.mapIndexed { i, word ->
        val examples = if (i == 0) primaryExamples else emptyList()
        val translations = if (i == 0) primaryTranslations else emptyList()
        val transcription = if (i == 0) this.transcription ?: "" else null
        val pos = if (i == 0) this.partOfSpeech ?: "" else null
        CardWordEntity(
            word = word,
            transcription = transcription,
            partOfSpeech = pos,
            translations = translations,
            examples = examples,
        )
    }
}

/**
 * Splits the given `phrase` using comma (i.e. '`,`') as separator.
 * Commas inside the parentheses (e.g. "`(x,y)`") are not considered.
 *
 * @param [phrase]
 * @return [List]
 */
internal fun fromDocumentCardTranslationToCommonWordDtoTranslation(phrase: String): List<String> {
    val parts = phrase.split(",")
    val res = mutableListOf<String>()
    var i = 0
    while (i < parts.size) {
        val pi = parts[i].trim()
        if (pi.isEmpty()) {
            i++
            continue
        }
        if (!pi.contains("(") || pi.contains(")")) {
            res.add(pi)
            i++
            continue
        }
        val sb = StringBuilder(pi)
        var j = i + 1
        while (j < parts.size) {
            val pj = parts[j].trim { it <= ' ' }
            if (pj.isEmpty()) {
                j++
                continue
            }
            sb.append(", ").append(pj)
            if (pj.contains(")")) {
                break
            }
            j++
        }
        if (sb.lastIndexOf(")") == -1) {
            res.add(pi)
            i++
            continue
        }
        res.add(sb.toString())
        i = j
        i++
    }
    return res
}

private fun AppConfig.status(answered: Int?): DocumentCardStatus = if (answered == null) {
    DocumentCardStatus.UNKNOWN
} else {
    if (answered >= this.defaultNumberOfRightAnswers) {
        DocumentCardStatus.LEARNED
    } else {
        DocumentCardStatus.IN_PROCESS
    }
}

private fun AppConfig.answered(status: DocumentCardStatus): Int = when (status) {
    DocumentCardStatus.UNKNOWN -> 0
    DocumentCardStatus.IN_PROCESS -> 1
    DocumentCardStatus.LEARNED -> this.defaultNumberOfRightAnswers
}