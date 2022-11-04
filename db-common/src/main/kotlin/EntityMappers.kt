package com.gitlab.sszuev.flashcards.common

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.gitlab.sszuev.flashcards.common.documents.CardStatus
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage

private val mapper: ObjectMapper = ObjectMapper()
private val detailsTypeReference: TypeReference<Map<Stage, Long>> =
    object : TypeReference<Map<Stage, Long>>() {}

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

fun toEntityDetails(fromDb: String?): Map<Stage, Long> {
    return if (fromDb.isNullOrBlank()) {
        emptyMap()
    } else try {
        mapper.readValue(fromDb, detailsTypeReference)
    } catch (e: JsonProcessingException) {
        emptyMap()
    }
}

fun toDbRecordDetails(details: Map<Stage, Long>): String {
    return try {
        mapper.writeValueAsString(details)
    } catch (e: JsonProcessingException) {
        throw IllegalStateException("Can't convert $details to string", e)
    }
}

/**
 * TODO: PG impl should contain JSON, not plain text
 */
fun toEntityTranslations(fromDb: String): List<String> {
    return splitIntoWords(fromDb)
}

/**
 * TODO: PG impl should contain JSON, not plain text
 */
fun toDbRecordTranslations(fromCore: List<String>): String {
    return fromCore.joinToString(",")
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