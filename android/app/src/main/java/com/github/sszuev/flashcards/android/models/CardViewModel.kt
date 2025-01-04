package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.Card
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.toCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardViewModel(
    private val repository: CardsRepository
) : ViewModel() {

    private val tag = "CardViewModel"

    val cads = mutableStateOf<List<Card>>(emptyList())
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    val selectedCardId = mutableStateOf<String?>(null)

    var sortField = mutableStateOf<String?>("name")
    var isAscending = mutableStateOf(true)

    fun loadCards(dictionaryId: String) {
        viewModelScope.launch {
            Log.d(tag, "load cards for dictionary = $dictionaryId")
            isLoading.value = true
            errorMessage.value = null
            cads.value = emptyList()
            try {
                withContext(Dispatchers.IO) {
                    cads.value = repository.getAll(dictionaryId).map { it.toCard() }
                }
            } catch (e: Exception) {
                errorMessage.value = "Failed to load cards: ${e.localizedMessage}"
                Log.e(tag, "Failed to load cards", e)
            } finally {
                isLoading.value = false
            }
        }
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
        cads.value = cads.value.sortedWith { a, b ->
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