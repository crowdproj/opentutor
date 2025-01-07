package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.State
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

    private val _isLoadSettingsInProgress = mutableStateOf(true)
    val isLoadSettingsInProgress: State<Boolean> get() = _isLoadSettingsInProgress
    private val _isSaveSettingsInProgress = mutableStateOf(true)
    private val _errorMessage = mutableStateOf<String?>(null)
    private val _settings = mutableStateOf<SettingsEntity?>(null)
    val settings: State<SettingsEntity?> = _settings

    fun loadSettings() {
        viewModelScope.launch {
            Log.d(tag, "load settings")
            _isLoadSettingsInProgress.value = true
            _errorMessage.value = null
            try {
                val settings = withContext(Dispatchers.IO) {
                    repository.get()
                }.toSettings()
                _settings.value = settings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get settings: ${e.localizedMessage}"
                Log.e(tag, "Failed to get settings", e)
            } finally {
                _isLoadSettingsInProgress.value = false
            }
        }
    }

    fun saveSettings(settingsEntity: SettingsEntity) {
        viewModelScope.launch {
            Log.d(tag, "save settings")
            _isSaveSettingsInProgress.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.update(settingsEntity.toSettingsResource())
                }
                _settings.value = settingsEntity
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save settings: ${e.localizedMessage}"
                Log.e(tag, "Failed to save settings", e)
            } finally {
                _isSaveSettingsInProgress.value = false
            }
        }
    }
}