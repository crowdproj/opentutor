package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.entities.DictionaryEntity
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository
import com.github.sszuev.flashcards.android.repositories.InvalidTokenException
import com.github.sszuev.flashcards.android.toDictionaryEntity
import com.github.sszuev.flashcards.android.toDictionaryResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class DictionariesViewModel(
    private val repository: DictionaryRepository,
    private val signOut: () -> Unit,
) : ViewModel() {

    private val tag = "DictionariesViewModel"

    private val _dictionaries = mutableStateOf<List<DictionaryEntity>>(emptyList())
    val dictionaries: State<List<DictionaryEntity>> get() = _dictionaries

    private val _isDictionariesLoading = mutableStateOf(true)
    private val _isUpdateInProgress = mutableStateOf(true)
    private val _isCreateInProgress = mutableStateOf(true)
    private val _isDeleteInProgress = mutableStateOf(true)
    private var _isDictionariesInitialized = false
    val isDictionariesLoading: State<Boolean> = _isDictionariesLoading
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _sortField = mutableStateOf<String?>("name")
    val sortField: State<String?> = _sortField
    private val _isAscending = mutableStateOf(true)
    val isAscending: State<Boolean> = _isAscending

    private val _selectedDictionaryIds = mutableStateOf<Set<String>>(emptySet())
    val selectedDictionaryIds: State<Set<String>> get() = _selectedDictionaryIds

    val selectedDictionariesList: List<DictionaryEntity>
        get() =
            _dictionaries.value.filter { it.dictionaryId in _selectedDictionaryIds.value }

    val languages = Locale.getAvailableLocales()
        .filterNot { it.language.isBlank() }
        .map {
            if (it.language == "en") {
                it.language to it.getDisplayLanguage(Locale.US)
            } else {
                it.language to "${it.getDisplayLanguage(Locale.US)} (${it.getDisplayLanguage(it)})"
            }
        }
        .sortedBy { it.second }
        .toMap()

    fun loadDictionariesInit(language: String) {
        if (_isDictionariesInitialized) {
            return
        }
        Log.i(tag, "Load dictionaries with locale=$language")
        loadDictionariesInternal(language)
        _isDictionariesInitialized = true
    }

    fun loadDictionaries() {
        Log.d(tag, "load dictionaries")
        loadDictionariesInternal()
    }

    private fun loadDictionariesInternal(language: String? = null) {
        viewModelScope.launch {
            _isDictionariesLoading.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    _dictionaries.value = repository
                        .getAll(language)
                        .map { it.toDictionaryEntity() }
                        .sortedBy { it.name }
                }
                _selectedDictionaryIds.value = emptySet()
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load dictionaries. Press HOME to refresh the page."
                Log.e(tag, "Failed to load dictionaries", e)
            } finally {
                _isDictionariesLoading.value = false
            }
        }
    }

    fun updateDictionary(dictionary: DictionaryEntity) {
        viewModelScope.launch {
            Log.d(tag, "update dictionary")
            _isUpdateInProgress.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.updateDictionary(dictionary.toDictionaryResource())
                }
                val dictionaries = _dictionaries.value.toMutableList()
                dictionaries.toList().forEachIndexed { index, it ->
                    if (it.dictionaryId == dictionary.dictionaryId) {
                        dictionaries.removeAt(index)
                        dictionaries.add(index, dictionary)
                        return@forEachIndexed
                    }
                }
                _dictionaries.value = dictionaries
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update dictionary. Press HOME to refresh the page."
                Log.e(tag, "Failed to update dictionary", e)
            } finally {
                _isUpdateInProgress.value = false
            }
        }
    }

    fun createDictionary(dictionary: DictionaryEntity) {
        viewModelScope.launch {
            Log.d(tag, "create dictionary")
            _isCreateInProgress.value = true
            _errorMessage.value = null
            try {
                val res = withContext(Dispatchers.IO) {
                    repository.createDictionary(dictionary.toDictionaryResource())
                }
                val dictionaries = _dictionaries.value.toMutableList()
                dictionaries.add(res.toDictionaryEntity())
                _dictionaries.value = dictionaries

                selectLast(checkNotNull(res.dictionaryId))
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create dictionary. Press HOME to refresh the page."
                Log.e(tag, "Failed to create dictionary", e)
            } finally {
                _isCreateInProgress.value = false
            }
        }
    }

    fun deleteDictionary(dictionaryId: String) {
        viewModelScope.launch {
            Log.d(tag, "delete dictionary")
            _isDeleteInProgress.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteDictionary(dictionaryId)
                }
                val dictionaries = _dictionaries.value.toMutableList()
                dictionaries.removeIf { it.dictionaryId == dictionaryId }
                _dictionaries.value = dictionaries
                val selectedDictionariesIds = _selectedDictionaryIds.value.toMutableSet()
                selectedDictionariesIds.removeIf { dictionaryId == it }
                _selectedDictionaryIds.value = selectedDictionariesIds
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete dictionary. Press HOME to refresh the page."
                Log.e(tag, "Failed to delete dictionary", e)
            } finally {
                _isDeleteInProgress.value = false
            }
        }
    }

    private fun selectLast(dictionaryId: String) {
        val currentSet = _selectedDictionaryIds.value.toMutableSet()
        currentSet.clear()
        currentSet.add(dictionaryId)
        _selectedDictionaryIds.value = currentSet
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

    fun dictionaryById(id: String): DictionaryEntity =
        checkNotNull(dictionaries.value.singleOrNull { it.dictionaryId == id }) {
            "Can't find dictionary by id = $id"
        }

    fun sortBy(field: String) {
        if (sortField.value == field) {
            _isAscending.value = !_isAscending.value
        } else {
            _sortField.value = field
            _isAscending.value = true
        }
        applySorting()
    }

    private fun applySorting() {
        _dictionaries.value = dictionaries.value.sortedWith { a, b ->
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