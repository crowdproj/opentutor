package com.github.sszuev.flashcards.android.repositories

import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import java.util.UUID

class DictionaryRepository(
    private val serverUri: String,
) {
    private val tag = "DictionaryRepository"

    suspend fun getAll(language: String? = null): List<DictionaryResource> {
        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Get all dictionaries with requestId=$requestId")
        val container =
            authPost<GetAllDictionariesResponse>("$serverUri/v1/api/dictionaries/get-all") {
                setBody(
                    GetAllDictionariesRequest(
                        requestType = "getAllDictionaries",
                        requestId = requestId,
                        locale = language,
                    )
                )
            }
        handleErrors(container)
        Log.d(
            tag,
            "Received response for requestId: $requestId, dictionaries count: ${container.dictionaries.size}"
        )
        return container.dictionaries
    }

    suspend fun updateDictionary(dictionary: DictionaryResource) {
        val requestId = UUID.randomUUID().toString()
        Log.d(
            tag,
            "Update dictionary with requestId=$requestId, dictionaryId=${dictionary.dictionaryId}"
        )
        val container =
            authPost<UpdateDictionaryResponse>("$serverUri/v1/api/dictionaries/update") {
                setBody(
                    UpdateDictionaryRequest(
                        requestType = "updateDictionary",
                        requestId = requestId,
                        dictionary = dictionary,
                    )
                )
            }
        handleErrors(container)
        Log.d(
            tag,
            "Successfully update dictionary with id=${dictionary.dictionaryId}, requestId=$requestId"
        )
    }

    suspend fun createDictionary(dictionary: DictionaryResource): DictionaryResource {
        val requestId = UUID.randomUUID().toString()
        Log.d(
            tag,
            "Create dictionary with requestId=$requestId"
        )
        val container =
            authPost<CreateDictionaryResponse>("$serverUri/v1/api/dictionaries/create") {
                setBody(
                    CreateDictionaryRequest(
                        requestType = "createDictionary",
                        requestId = requestId,
                        dictionary = dictionary,
                    )
                )
            }
        handleErrors(container)
        val res = container.dictionary
        Log.d(
            tag,
            "Successfully create dictionary with id=${res.dictionaryId}, requestId=$requestId"
        )
        return res
    }

    suspend fun deleteDictionary(dictionaryId: String) {
        val requestId = UUID.randomUUID().toString()
        Log.d(
            tag,
            "Delete dictionary with requestId=$requestId"
        )
        val container =
            authPost<DeleteDictionaryResponse>("$serverUri/v1/api/dictionaries/delete") {
                setBody(
                    DeleteDictionaryRequest(
                        requestType = "deleteDictionary",
                        requestId = requestId,
                        dictionaryId = dictionaryId,
                    )
                )
            }
        handleErrors(container)
        Log.d(
            tag,
            "Successfully delete dictionary with id=${dictionaryId}, requestId=$requestId"
        )
    }

}

@Suppress("unused")
@Serializable
private class GetAllDictionariesRequest(
    override val requestType: String,
    override val requestId: String,
    val locale: String? = null,
) : BaseRequest

@Serializable
private data class GetAllDictionariesResponse(
    override val requestId: String,
    val dictionaries: List<DictionaryResource>,
    override val errors: List<ErrorResource>? = null,
) : BaseResponse

@Serializable
private data class UpdateDictionaryRequest(
    override val requestType: String,
    override val requestId: String,
    val dictionary: DictionaryResource,
) : BaseRequest

@Serializable
private data class UpdateDictionaryResponse(
    override val requestId: String,
    val dictionary: DictionaryResource,
    override val errors: List<ErrorResource>? = null,
) : BaseResponse

@Serializable
private data class CreateDictionaryRequest(
    override val requestType: String,
    override val requestId: String,
    val dictionary: DictionaryResource,
) : BaseRequest

@Serializable
private data class CreateDictionaryResponse(
    override val requestId: String,
    val dictionary: DictionaryResource,
    override val errors: List<ErrorResource>? = null,
) : BaseResponse

@Serializable
private data class DeleteDictionaryRequest(
    override val requestType: String,
    override val requestId: String,
    val dictionaryId: String,
) : BaseRequest

@Serializable
private data class DeleteDictionaryResponse(
    override val requestId: String,
    override val errors: List<ErrorResource>? = null,
) : BaseResponse
