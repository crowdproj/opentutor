package com.github.sszuev.flashcards.android.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository

class ViewModelFactory(
    private val repository: DictionaryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            return DictionaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}