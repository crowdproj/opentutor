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
import com.github.sszuev.flashcards.android.repositories.TranslationRepository
import com.github.sszuev.flashcards.android.toCard
import com.github.sszuev.flashcards.android.toCardResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CardViewModel(
    private val cardsRepository: CardsRepository,
    private val ttsRepository: TTSRepository,
    private val translationRepository: TranslationRepository,
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

    private val _isCardFetching = mutableStateOf(true)
    val isCardFetching: State<Boolean> = _isCardFetching
    private val _fetchedCard = mutableStateOf<CardEntity?>(null)
    val fetchedCard: State<CardEntity?> = _fetchedCard

    private val _isCardCreating = mutableStateOf(true)

    private val _isCardDeleting = mutableStateOf(true)

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
                                cardId = checkNotNull(card.cardId),
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

    fun createCard(card: CardEntity) {
        viewModelScope.launch {
            Log.d(tag, "create card")
            _isCardCreating.value = true
            _errorMessage.value = null
            try {
                val res = withContext(Dispatchers.IO) {
                    cardsRepository.createCard(card.toCardResource())
                }
                val cards = _cards.value.toMutableList()
                cards.add(res.toCard())
                _cards.value = cards
                _selectedCardId.value = res.cardId
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create card: ${e.localizedMessage}"
                Log.e(tag, "Failed to create card", e)
            } finally {
                _isCardCreating.value = false
            }
        }
    }

    fun fetchCard(word: String, sourceLang: String, targetLang: String) {
        if (word.isBlank()) {
            _fetchedCard.value = null
            _isCardFetching.value = false
            return
        }
        viewModelScope.launch {
            Log.d(tag, "Fetch card data ['$word'; $sourceLang -> $targetLang]")
            _isCardFetching.value = true
            _errorMessage.value = null
            try {
                val fetched = withContext(Dispatchers.IO) {
                    translationRepository.fetch(
                        query = word,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                    )
                }.toCard()
                _fetchedCard.value = fetched
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch card data: ${e.localizedMessage}"
                Log.e(tag, "Failed to fetch card data", e)
            } finally {
                _isCardFetching.value = false
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            Log.d(tag, "delete card")
            _isCardDeleting.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    cardsRepository.deleteCard(cardId)
                }
                val dictionaries = _cards.value.toMutableList()
                dictionaries.removeIf { it.cardId == cardId }
                _cards.value = dictionaries
                if (_selectedCardId.value == cardId) {
                    _selectedCardId.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete card: ${e.localizedMessage}"
                Log.e(tag, "Failed to delete card", e)
            } finally {
                _isCardDeleting.value = false
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

    fun clearFetchedCard() {
        _fetchedCard.value = null
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