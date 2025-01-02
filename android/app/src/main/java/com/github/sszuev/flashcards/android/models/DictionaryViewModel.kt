package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.sszuev.flashcards.android.Dictionary
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository
import com.github.sszuev.flashcards.android.toDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryViewModel(
    private val repository: DictionaryRepository
) : ViewModel() {

    private val tag = "DictionaryViewModel"

    val dictionaries = mutableStateOf<List<Dictionary>>(emptyList())
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    suspend fun loadDictionaries() = withContext(Dispatchers.IO) {
        Log.d(tag, "load dictionaries")
        isLoading.value = true
        errorMessage.value = null
        try {
            dictionaries.value = repository.getAll().map { it.toDictionary() }
        } catch (e: Exception) {
            errorMessage.value = "Failed to load dictionaries: ${e.localizedMessage}"
        } finally {
            isLoading.value = false
        }
    }
}