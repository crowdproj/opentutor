package com.gitlab.sszuev.flashcards.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse

/**
 * System-wide mapper.
 */
internal val jacksonMapper = ObjectMapper()

fun serialize(response: Any): String = jacksonMapper.writeValueAsString(response)

@Suppress("UNCHECKED_CAST")
fun <R : BaseResponse> deserializeResponse(json: String): R =
    jacksonMapper.readValue(json, BaseResponse::class.java) as R

@Suppress("UNCHECKED_CAST")
fun <R : BaseRequest> deserializeRequest(json: String): R =
    jacksonMapper.readValue(json, BaseRequest::class.java) as R