package com.gitlab.sszuev.flashcards.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule


private val mapper = ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    .registerModule(KotlinModule.Builder().build())

private val cardWordsTypeReference: TypeReference<List<CommonWordDto>> =
    object : TypeReference<List<CommonWordDto>>() {}

fun parseDictionaryDetailsJson(json: String): CommonDictionaryDetailsDto {
    return mapper.readValue(json, CommonDictionaryDetailsDto::class.java)
}

fun CommonDictionaryDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

fun parseCardDetailsJson(json: String): CommonCardDetailsDto {
    return mapper.readValue(json, CommonCardDetailsDto::class.java)
}

fun parseUserDetailsJson(json: String): CommonUserDetailsDto {
    return mapper.readValue(json, CommonUserDetailsDto::class.java)
}

fun CommonCardDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

fun CommonUserDetailsDto.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

fun parseCardWordsJson(json: String): List<CommonWordDto> {
    return mapper.readValue(json, cardWordsTypeReference)
}

fun List<CommonWordDto>.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

// mutable map due to jackson's serialization issue
data class CommonDictionaryDetailsDto(private val content: MutableMap<String, Any> = mutableMapOf()) :
    MutableMap<String, Any> by content

data class CommonCardDetailsDto(private val content: MutableMap<String, Any> = mutableMapOf()) :
    MutableMap<String, Any> by content

data class CommonUserDetailsDto(private val content: MutableMap<String, Any> = mutableMapOf()) :
    MutableMap<String, Any> by content

data class CommonWordDto(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<CommonExampleDto> = emptyList(),
    val translations: List<List<String>> = emptyList(),
    val primary: Boolean = false,
)

data class CommonExampleDto(
    val text: String,
    val translation: String? = null,
)