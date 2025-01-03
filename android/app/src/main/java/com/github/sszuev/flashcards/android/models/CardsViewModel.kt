package com.github.sszuev.flashcards.android.models

import androidx.lifecycle.ViewModel
import com.github.sszuev.flashcards.android.repositories.CardsRepository

class CardsViewModel(
    private val repository: CardsRepository
) : ViewModel() {
}