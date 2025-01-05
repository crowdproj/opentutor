package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.toCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardViewModel(
    private val repository: CardsRepository
) : ViewModel() {

    private val tag = "CardViewModel"

    private val _cads = mutableStateOf<List<CardEntity>>(emptyList())
    val cads: State<List<CardEntity>> = _cads
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage
    private val _selectedCardId = mutableStateOf<String?>(null)
    val selectedCardId: State<String?> = _selectedCardId

    private val _sortField = mutableStateOf<String?>("name")
    var sortField: State<String?> = _sortField
    private var _isAscending = mutableStateOf(true)
    var isAscending: State<Boolean> = _isAscending

    fun loadCards(dictionaryId: String) {
        viewModelScope.launch {
            Log.d(tag, "load cards for dictionary = $dictionaryId")
            _isLoading.value = true
            _errorMessage.value = null
            _cads.value = emptyList()
            try {
                withContext(Dispatchers.IO) {
                    _cads.value = repository.getAll(dictionaryId).map { it.toCard() }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards: ${e.localizedMessage}"
                Log.e(tag, "Failed to load cards", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCard(cardId: String?) {
        _selectedCardId.value = cardId
    }

    fun sortBy(field: String) {
        if (_sortField.value == field) {
            _isAscending.value = !_isAscending.value
        } else {
            _sortField.value = field
            _isAscending.value = true
        }
        applySorting()
    }

    private fun applySorting() {
        _cads.value = cads.value.sortedWith { a, b ->
            val result = when (sortField.value) {
                "word" -> a.word.compareTo(b.word)
                "translation" -> a.translation.compareTo(b.translation)
                "status" -> a.answered.compareTo(b.answered)
                else -> 0
            }
            if (isAscending.value) result else -result
        }
    }
}