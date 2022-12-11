package com.gitlab.sszuev.flashcards.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.gitlab.sszuev.flashcards.common.documents.CardStatus
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId


private val mapper = ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    .registerModule(KotlinModule.Builder().build())

private val cardWordsTypeReference: TypeReference<List<CommonWordDto>> =
    object : TypeReference<List<CommonWordDto>>() {}

@Suppress("UNCHECKED_CAST")
fun parseUserDetailsJson(json: String): CommonUserDetailsDto {
    return mapper.readValue(json, CommonUserDetailsDto::class.java)
}

fun CommonUserDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

@Suppress("UNCHECKED_CAST")
fun parseDictionaryDetailsJson(json: String): CommonDictionaryDetailsDto {
    return mapper.readValue(json, CommonDictionaryDetailsDto::class.java)
}

fun CommonDictionaryDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

fun parseCardDetailsJson(json: String): CommonCardDetailsDto {
    return mapper.readValue(json, CommonCardDetailsDto::class.java)
}

fun CommonCardDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

fun parseCardWordsJson(json: String): List<CommonWordDto> {
    return mapper.readValue(json, cardWordsTypeReference)
}

fun List<CommonWordDto>.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

fun requireNew(entity: CardEntity) {
    require(entity.cardId == CardId.NONE) { "Specified card-id=${entity.cardId}, expected=none" }
    require(entity.dictionaryId != DictionaryId.NONE) { "Dictionary id is required" }
}

fun requireExiting(entity: CardEntity) {
    require(entity.cardId.asString().isNotBlank()) { "No card-id specified" }
    require(entity.dictionaryId != DictionaryId.NONE) { "Dictionary id is required" }
}

fun Id.asLong(): Long = if (this.asString().matches("\\d+".toRegex())) {
    this.asString().toLong()
} else {
    throw IllegalArgumentException("Wrong id specified: $this")
}

@Deprecated("to remove")
fun toEntityTranslations(fromDocument: String): List<String> {
    return splitIntoWords(fromDocument)
}

@Deprecated("to remove")
fun toDocumentTranslations(fromEntity: List<String>): String {
    return fromEntity.joinToString(",")
}

fun CommonWordDto.toDocumentTranslations(): List<String> {
    return translations.map { it.joinToString(",") }
}

fun CommonWordDto.toDocumentExamples(): List<String> {
    return examples.map { if (it.translation != null) "${it.example} -- ${it.translation}" else it.example }
}

fun DocumentCard.toCommonWordDtoList(): List<CommonWordDto> {
    val forms = this.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val primaryTranslations = this.translations.map { splitIntoWords(it) }
    val primaryExamples = this.examples.map { example ->
        val parts = example.split(" -- ").filter { it.isNotEmpty() }
        val (e, t) = if (parts.size == 2) {
            parts[0] to parts[1]
        } else {
            example to null
        }
        CommonExampleDto(example = e, translation = t)
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
internal fun splitIntoWords(phrase: String): List<String> {
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

fun SysConfig.status(answered: Int?): CardStatus {
    return if (answered == null) {
        CardStatus.UNKNOWN
    } else {
        if (answered >= this.numberOfRightAnswers) {
            CardStatus.LEARNED
        } else {
            CardStatus.IN_PROCESS
        }
    }
}

fun SysConfig.answered(status: CardStatus): Int {
    return when (status) {
        CardStatus.UNKNOWN -> 0
        CardStatus.IN_PROCESS -> 1
        CardStatus.LEARNED -> this.numberOfRightAnswers
    }
}

data class CommonUserDetailsDto(private val content: Map<String, Any>) : Map<String, Any> by content

data class CommonDictionaryDetailsDto(private val content: Map<String, Any>) : Map<String, Any> by content

data class CommonCardDetailsDto(private val content: Map<String, Any>) : Map<String, Any> by content

data class CommonWordDto(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<CommonExampleDto> = emptyList(),
    val translations: List<List<String>> = emptyList(),
)

data class CommonExampleDto(
    val example: String,
    val translation: String? = null,
)