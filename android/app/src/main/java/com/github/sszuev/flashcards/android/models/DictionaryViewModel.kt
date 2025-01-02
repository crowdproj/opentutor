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

    var sortField = mutableStateOf<String?>("name")
    var isAscending = mutableStateOf(true)

    private val _selectedDictionaryIds = mutableStateOf<Set<String>>(emptySet())
    val selectedDictionaryIds: Set<String> get() = _selectedDictionaryIds.value

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

    fun toggleSelection(dictionaryId: String, isSelected: Boolean) {
        val currentSet = _selectedDictionaryIds.value.toMutableSet()
        if (isSelected) {
            currentSet.add(dictionaryId)
        } else {
            currentSet.remove(dictionaryId)
        }
        _selectedDictionaryIds.value = currentSet
    }

    fun sortBy(field: String) {
        if (sortField.value == field) {
            isAscending.value = !isAscending.value
        } else {
            sortField.value = field
            isAscending.value = true
        }
        applySorting()
    }

    private fun applySorting() {
        dictionaries.value = dictionaries.value.sortedWith { a, b ->
            val result = when (sortField.value) {
                "name" -> a.name.compareTo(b.name)
                "sourceLanguage" -> a.sourceLanguage.compareTo(b.sourceLanguage)
                "targetLanguage" -> a.targetLanguage.compareTo(b.targetLanguage)
                "totalWords" -> a.totalWords.compareTo(b.totalWords)
                "learnedWords" -> a.learnedWords.compareTo(b.learnedWords)
                else -> 0
            }
            if (isAscending.value) result else -result
        }
    }

}