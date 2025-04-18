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
        if (modelClass.isAssignableFrom(DictionariesViewModel::class.java)) {
            return DictionariesViewModel(repository, signOut) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CardsViewModelFactory(
    private val cardsRepository: CardsRepository,
    private val translationRepository: TranslationRepository,
    private val signOut: () -> Unit,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardsViewModel::class.java)) {
            return CardsViewModel(
                cardsRepository = cardsRepository,
                translationRepository = translationRepository,
                signOut = signOut,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TutorViewModelFactory(
    private val cardsRepository: CardsRepository,
    private val signOut: () -> Unit,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorViewModel::class.java)) {
            return TutorViewModel(
                cardsRepository = cardsRepository,
                signOut = signOut,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TTSViewModelFactory(
    private val context: Application,
    private val ttsRepository: TTSRepository,
    private val signOut: () -> Unit,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TTSViewModel::class.java)) {
            return TTSViewModel(
                context = context,
                ttsRepository = ttsRepository,
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