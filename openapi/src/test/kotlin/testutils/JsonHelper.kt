package com.gitlab.sszuev.flashcards.api.testutils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse

/**
 * System-wide mapper.
 */
private val jacksonMapper = ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(JavaTimeModule())

internal fun serialize(response: Any): String = jacksonMapper.writeValueAsString(response)

@Suppress("UNCHECKED_CAST")
internal fun <R : BaseResponse> deserializeResponse(json: String): R =
    jacksonMapper.readValue(json, BaseResponse::class.java) as R

@Suppress("UNCHECKED_CAST")
internal fun <R : BaseRequest> deserializeRequest(json: String): R =
    jacksonMapper.readValue(json, BaseRequest::class.java) as R

internal fun String.normalize() = replace("\\s+".toRegex(), "")