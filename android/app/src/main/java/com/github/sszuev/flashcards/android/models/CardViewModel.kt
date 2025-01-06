package com.github.sszuev.flashcards.android.models

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.repositories.TTSRepository
import com.github.sszuev.flashcards.android.toCard
import com.github.sszuev.flashcards.android.toCardResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CardViewModel(
    private val cardsRepository: CardsRepository,
    private val ttsRepository: TTSRepository,
) : ViewModel() {

    private val tag = "CardViewModel"

    private val _cards = mutableStateOf<List<CardEntity>>(emptyList())
    val cards: State<List<CardEntity>> = _cards
    private val _isCardsLoading = mutableStateOf(true)
    val isCardsLoading: State<Boolean> = _isCardsLoading
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage
    private val _selectedCardId = mutableStateOf<String?>(null)
    val selectedCardId: State<String?> = _selectedCardId

    private val _sortField = mutableStateOf<String?>("name")
    val sortField: State<String?> = _sortField
    private val _isAscending = mutableStateOf(true)
    val isAscending: State<Boolean> = _isAscending

    private val _audioResources = mutableStateMapOf<String, ByteArray?>()
    private val _isAudioLoading = mutableStateOf(true)
    val isAudioLoading: State<Boolean> = _isCardsLoading
    private val _isAudioPlaying = mutableStateOf(false)
    val isAudioPlaying: State<Boolean> get() = _isAudioPlaying

    private val _isCardUpdating = mutableStateOf(true)

    val selectedCard: CardEntity?
        get() = if (_selectedCardId.value == null) null else {
            _cards.value.singleOrNull { it.cardId == _selectedCardId.value }
        }

    fun loadCards(dictionaryId: String) {
        viewModelScope.launch {
            Log.d(tag, "load cards for dictionary = $dictionaryId")
            _isCardsLoading.value = true
            _errorMessage.value = null
            _cards.value = emptyList()
            try {
                withContext(Dispatchers.IO) {
                    _cards.value = cardsRepository.getAll(dictionaryId).map { it.toCard() }
                }
                _selectedCardId.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards: ${e.localizedMessage}"
                Log.e(tag, "Failed to load cards", e)
            } finally {
                _isCardsLoading.value = false
            }
        }
    }

    fun updateCard(card: CardEntity) {
        viewModelScope.launch {
            Log.d(tag, "Update card with id = ${card.cardId}")
            _isCardUpdating.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    cardsRepository.updateCard(card.toCardResource())
                }
                val cards = _cards.value.toMutableList()
                _cards.value.forEachIndexed { index, entity ->
                    if (entity.cardId == card.cardId) {
                        if (entity.audioId != card.audioId) {
                            _loadAudio(
                                cardId = card.cardId,
                                audioResourceId = card.audioId,
                                onLoaded = {}
                            )
                        }
                        cards[index] = card
                        return@forEachIndexed
                    }
                }
                _cards.value = cards
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards: ${e.localizedMessage}"
                Log.e(tag, "Failed to load cards", e)
            } finally {
                _isCardUpdating.value = false
            }
        }
    }

    fun loadAudio(cardId: String, audioResourceId: String, onLoaded: () -> Unit) {
        if (_audioResources.contains(cardId)) {
            if (_audioResources[cardId] != null) {
                onLoaded()
            }
            return
        }
        viewModelScope.launch {
            _loadAudio(cardId, audioResourceId, onLoaded)
        }
    }

    @Suppress("FunctionName")
    private suspend fun _loadAudio(cardId: String, audioResourceId: String, onLoaded: () -> Unit) {
        Log.d(tag, "load audio for card = $cardId")
        _isAudioLoading.value = true
        _errorMessage.value = null
        val lang = audioResourceId.substringBefore(":")
        val word = audioResourceId.substringAfter(":")
        try {
            val resource = withContext(Dispatchers.IO) {
                ttsRepository.get(lang, word)
            }
            _audioResources[cardId] = resource
            if (resource != null) {
                onLoaded()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load audio: ${e.localizedMessage}"
            Log.e(tag, "Failed to load audio", e)
        } finally {
            _isAudioLoading.value = false
        }
    }

    fun playAudio(cardId: String) {
        val audioData = _audioResources[cardId] ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempFile = File.createTempFile("temp_audio.", ".mp3")
                Log.d(tag, "Create temp file $tempFile")
                tempFile.outputStream().use {
                    it.write(audioData)
                }

                withContext(Dispatchers.Main) {
                    _isAudioPlaying.value = true
                    MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepareAsync()
                        setOnPreparedListener { start() }
                        setOnCompletionListener {
                            it.release()
                            tempFile.delete()
                            _isAudioPlaying.value = false
                        }
                        setOnErrorListener { mp, what, extra ->
                            mp.release()
                            _isAudioPlaying.value = false
                            true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to play audio: ${e.localizedMessage}", e)
            }
        }
    }

    fun isAudioLoaded(cardId: String): Boolean {
        return _audioResources.contains(cardId)
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
        _cards.value = cards.value.sortedWith { a, b ->
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