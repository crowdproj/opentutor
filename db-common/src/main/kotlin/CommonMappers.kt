package com.gitlab.sszuev.flashcards.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.gitlab.sszuev.flashcards.model.domain.Stage


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