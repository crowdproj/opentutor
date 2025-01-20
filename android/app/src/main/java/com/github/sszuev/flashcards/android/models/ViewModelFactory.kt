package com.github.sszuev.flashcards.android.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository
import com.github.sszuev.flashcards.android.repositories.SettingsRepository
import com.github.sszuev.flashcards.android.repositories.TTSRepository
import com.github.sszuev.flashcards.android.repositories.TranslationRepository

class DictionariesViewModelFactory(
    private val repository: DictionaryRepository,
    private val signOut: () -> Unit,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            return DictionaryViewModel(repository, signOut) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CardsViewModelFactory(
    private val context: Application,
    private val cardsRepository: CardsRepository,
    private val ttsRepository: TTSRepository,
    private val translationRepository: TranslationRepository,
    private val signOut: () -> Unit,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            return CardViewModel(
                context = context,
                cardsRepository = cardsRepository,
                ttsRepository = ttsRepository,
                translationRepository = translationRepository,
                signOut = signOut,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val signOut: () -> Unit,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository, signOut) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}