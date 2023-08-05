package com.gitlab.sszuev.flashcards.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.Stage
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant

fun systemNow(): java.time.LocalDateTime = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toLocalDateTime()

fun kotlinx.datetime.Instant?.asJava(): java.time.LocalDateTime =
    (this?:kotlinx.datetime.Instant.NONE).toJavaInstant().atOffset(java.time.ZoneOffset.UTC).toLocalDateTime()

fun java.time.LocalDateTime?.asKotlin(): kotlinx.datetime.Instant =
    this?.toInstant(java.time.ZoneOffset.UTC)?.toKotlinInstant() ?: kotlinx.datetime.Instant.NONE

private val mapper = ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    .registerModule(KotlinModule.Builder().build())

private val cardWordsTypeReference: TypeReference<List<CommonWordDto>> =
    object : TypeReference<List<CommonWordDto>>() {}

fun parseUserDetailsJson(json: String): CommonUserDetailsDto {
    return mapper.readValue(json, CommonUserDetailsDto::class.java)
}

fun CommonUserDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

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

fun Map<Stage, Long>.toCommonCardDtoDetails(): CommonCardDetailsDto =
    CommonCardDetailsDto(this.mapKeys { it.key.name }.mapValues { it.value.toString() })

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
    val text: String,
    val translation: String? = null,
)