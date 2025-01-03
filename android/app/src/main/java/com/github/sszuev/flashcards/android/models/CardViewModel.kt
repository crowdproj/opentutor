package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.sszuev.flashcards.android.Card
import com.github.sszuev.flashcards.android.Dictionary
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.toCard
import com.github.sszuev.flashcards.android.toDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CardViewModel(
    private val repository: CardsRepository
) : ViewModel() {

    private val tag = "CardViewModel"

    val cads = mutableStateOf<List<Card>>(emptyList())
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    suspend fun loadCards(dictionaryId: String) = withContext(Dispatchers.IO) {
        Log.d(tag, "load cards for dictionary = $dictionaryId")
        isLoading.value = true
        errorMessage.value = null
        cads.value = emptyList()
        try {
            cads.value = repository.getAll(dictionaryId).map { it.toCard() }
        } catch (e: Exception) {
            errorMessage.value = "Failed to load cards: ${e.localizedMessage}"
            Log.e(tag, "Failed to load cards", e)
        } finally {
            isLoading.value = false
        }
    }
}