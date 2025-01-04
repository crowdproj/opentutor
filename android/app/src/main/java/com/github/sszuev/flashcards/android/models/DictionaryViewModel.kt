package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.Dictionary
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository
import com.github.sszuev.flashcards.android.toDictionary
import com.github.sszuev.flashcards.android.toDictionaryResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DictionaryViewModel(
    private val repository: DictionaryRepository
) : ViewModel() {

    private val tag = "DictionaryViewModel"

    val dictionaries = mutableStateOf<List<Dictionary>>(emptyList())
    val isDictionariesLoading = mutableStateOf(true)
    val isUpdateInProgress = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    var sortField = mutableStateOf<String?>("name")
    var isAscending = mutableStateOf(true)

    private val _selectedDictionaryIds = mutableStateOf<Set<String>>(emptySet())
    val selectedDictionaryIds: Set<String> get() = _selectedDictionaryIds.value

    fun loadDictionaries() {
        viewModelScope.launch {
            Log.d(tag, "load dictionaries")
            isDictionariesLoading.value = true
            errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    dictionaries.value = repository.getAll().map { it.toDictionary() }
                }
            } catch (e: Exception) {
                errorMessage.value = "Failed to load dictionaries: ${e.localizedMessage}"
                Log.e(tag, "Failed to load dictionaries", e)
            } finally {
                isDictionariesLoading.value = false
            }
        }
    }

    fun updateDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            Log.d(tag, "update dictionary")
            isUpdateInProgress.value = true
            errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.updateDictionary(dictionary.toDictionaryResource())
                }
                val dictionaries = this@DictionaryViewModel.dictionaries.value.toMutableList()
                dictionaries.toList().forEachIndexed { index, it ->
                    if (it.dictionaryId == dictionary.dictionaryId) {
                        dictionaries.removeAt(index)
                        dictionaries.add(index, dictionary)
                    }
                }
                this@DictionaryViewModel.dictionaries.value = dictionaries
            } catch (e: Exception) {
                errorMessage.value = "Failed to update dictionary: ${e.localizedMessage}"
                Log.e(tag, "Failed to update dictionary", e)
            } finally {
                isUpdateInProgress.value = false
            }
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