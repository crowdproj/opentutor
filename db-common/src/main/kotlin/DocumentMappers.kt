package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentCardStatus

fun CommonWordDto.toDocumentTranslations(): List<String> {
    return translations.map { it.joinToString(",") }
}

fun CommonWordDto.toDocumentExamples(): List<String> {
    return examples.map { if (it.translation != null) "${it.text} -- ${it.translation}" else it.text }
}

fun DocumentCard.toCommonWordDtoList(): List<CommonWordDto> {
    val forms = this.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val primaryTranslations = this.translations.map { fromDocumentCardTranslationToCommonWordDtoTranslation(it) }
    val primaryExamples = this.examples.map { example ->
        val parts = example.split(" -- ").filter { it.isNotEmpty() }
        val (e, t) = if (parts.size == 2) {
            parts[0] to parts[1]
        } else {
            example to null
        }
        CommonExampleDto(text = e, translation = t)
    }
    return forms.mapIndexed { i, word ->
        val examples = if (i == 0) primaryExamples else emptyList()
        val translations = if (i == 0) primaryTranslations else emptyList()
        val transcription = if (i == 0) this.transcription ?: "" else null
        val pos = if (i == 0) this.partOfSpeech ?: "" else null
        CommonWordDto(
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

fun SysConfig.status(answered: Int?): DocumentCardStatus {
    return if (answered == null) {
        DocumentCardStatus.UNKNOWN
    } else {
        if (answered >= this.numberOfRightAnswers) {
            DocumentCardStatus.LEARNED
        } else {
            DocumentCardStatus.IN_PROCESS
        }
    }
}

fun SysConfig.answered(status: DocumentCardStatus): Int {
    return when (status) {
        DocumentCardStatus.UNKNOWN -> 0
        DocumentCardStatus.IN_PROCESS -> 1
        DocumentCardStatus.LEARNED -> this.numberOfRightAnswers
    }
}