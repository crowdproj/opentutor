package com.gitlab.sszuev.flashcards.api.testutils

import com.fasterxml.jackson.annotation.JsonInclude
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * System-wide mapper.
 */

private val jacksonMapper = JsonMapper.builder()
    .addModule(KotlinModule.Builder().build())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
    .build()

internal fun serialize(response: Any): String = jacksonMapper.writeValueAsString(response)

@Suppress("UNCHECKED_CAST")
internal fun <R : BaseResponse> deserializeResponse(json: String): R =
    jacksonMapper.readValue(json, BaseResponse::class.java) as R

@Suppress("UNCHECKED_CAST")
internal fun <R : BaseRequest> deserializeRequest(json: String): R =
    jacksonMapper.readValue(json, BaseRequest::class.java) as R

internal fun String.normalize() = replace("\\s+".toRegex(), "")