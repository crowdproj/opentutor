package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.repositories.InvalidTokenException
import com.github.sszuev.flashcards.android.repositories.SettingsRepository
import com.github.sszuev.flashcards.android.toSettingsEntity
import com.github.sszuev.flashcards.android.toSettingsResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val signOut: () -> Unit,
) : ViewModel() {
    private val tag = "SettingsViewModel"

    private val _isLoadSettingsInProgress = mutableStateOf(true)
    val isLoadSettingsInProgress: State<Boolean> get() = _isLoadSettingsInProgress
    private val _isSaveSettingsInProgress = mutableStateOf(true)
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage
    private val _settings = mutableStateOf<SettingsEntity?>(null)
    val settings: State<SettingsEntity?> = _settings

    fun loadSettings() {
        if (_settings.value != null) {
            return
        }

        viewModelScope.launch {
            Log.d(tag, "load settings")
            _isLoadSettingsInProgress.value = true
            _errorMessage.value = null
            try {
                val settings = withContext(Dispatchers.IO) {
                    repository.get()
                }.toSettingsEntity()
                _settings.value = settings
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get settings. Press HOME to refresh the page."
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
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save settings. Press HOME to refresh the page."
                Log.e(tag, "Failed to save settings", e)
            } finally {
                _isSaveSettingsInProgress.value = false
            }
        }
    }
}