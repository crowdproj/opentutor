package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.repositories.SettingsRepository
import com.github.sszuev.flashcards.android.toSettings
import com.github.sszuev.flashcards.android.toSettingsResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val tag = "SettingsViewModel"

    val isLoadSettingsInProgress = mutableStateOf(true)
    val isSaveSettingsInProgress = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)
    val settings = mutableStateOf<SettingsEntity?>(null)

    fun loadSettings() {
        viewModelScope.launch {
            Log.d(tag, "load settings")
            isLoadSettingsInProgress.value = true
            errorMessage.value = null
            try {
                val settings = withContext(Dispatchers.IO) {
                    repository.get()
                }.toSettings()
                this@SettingsViewModel.settings.value = settings
            } catch (e: Exception) {
                errorMessage.value = "Failed to get settings: ${e.localizedMessage}"
                Log.e(tag, "Failed to get settings", e)
            } finally {
                isLoadSettingsInProgress.value = false
            }
        }
    }

    fun saveSettings(settingsEntity: SettingsEntity) {
        viewModelScope.launch {
            Log.d(tag, "save settings")
            isSaveSettingsInProgress.value = true
            errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.update(settingsEntity.toSettingsResource())
                }
                this@SettingsViewModel.settings.value = settingsEntity
            } catch (e: Exception) {
                errorMessage.value = "Failed to save settings: ${e.localizedMessage}"
                Log.e(tag, "Failed to save settings", e)
            } finally {
                isSaveSettingsInProgress.value = false
            }
        }
    }
}