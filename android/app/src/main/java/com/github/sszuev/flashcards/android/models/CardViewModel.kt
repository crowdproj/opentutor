package com.github.sszuev.flashcards.android.models

import android.media.MediaPlayer
import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.PLAY_AUDIO_EMERGENCY_TIMEOUT_MS
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.repositories.InvalidTokenException
import com.github.sszuev.flashcards.android.repositories.TTSRepository
import com.github.sszuev.flashcards.android.repositories.TranslationRepository
import com.github.sszuev.flashcards.android.toCardEntity
import com.github.sszuev.flashcards.android.toCardResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

class CardViewModel(
    private val cardsRepository: CardsRepository,
    private val ttsRepository: TTSRepository,
    private val translationRepository: TranslationRepository,
    private val signOut: () -> Unit,
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

    @Suppress("PrivatePropertyName")
    private val NULL_AUDIO_MARKER = ByteArray(0)
    private val _audioResources = object : LruCache<String, ByteArray?>(1024) {
        fun getNullable(key: String): ByteArray? {
            return when (val result = get(key)) {
                NULL_AUDIO_MARKER -> null
                else -> result
            }
        }

        fun putNullable(key: String, value: ByteArray?) {
            put(key, value ?: NULL_AUDIO_MARKER)
        }
    }
    private val _isAudioLoading = mutableStateMapOf<String, Boolean>()
    private val _isAudioPlaying = mutableStateMapOf<String, Boolean>()

    private val _isCardUpdating = mutableStateOf(true)

    private val _isCardFetching = mutableStateOf(true)
    val isCardFetching: State<Boolean> get() = _isCardFetching
    private val _fetchedCard = mutableStateOf<CardEntity?>(null)
    val fetchedCard: State<CardEntity?> get() = _fetchedCard

    private val _isCardCreating = mutableStateOf(true)

    private val _isCardDeleting = mutableStateOf(true)

    private val _isCardResetting = mutableStateOf(true)

    private val _isCardsDeckLoading = mutableStateOf(true)
    val isCardsDeckLoading: State<Boolean> get() = _isCardsDeckLoading
    private val _stageShowCurrentDeckCardIndex = mutableIntStateOf(0)
    val stageShowCurrentDeckCardIndex: State<Int> get() = _stageShowCurrentDeckCardIndex
    private val _cardsDeck = mutableStateOf<List<CardEntity>>(emptyList())
    val cardsDeck: State<List<CardEntity>> = _cardsDeck
    private val _wrongAnsweredCardDeckIds = mutableStateOf<Set<String>>(emptySet())
    val wrongAnsweredCardDeckIds: State<Set<String>> = _wrongAnsweredCardDeckIds

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
                    _cards.value = cardsRepository
                        .getAll(dictionaryId)
                        .map { it.toCardEntity() }
                        .sortedBy { it.word }
                }
                _selectedCardId.value = null
            } catch (e: InvalidTokenException) {
                signOut()
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
            } catch (e: InvalidTokenException) {
                signOut()
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
                cards.add(res.toCardEntity())
                _cards.value = cards
                _selectedCardId.value = res.cardId
            } catch (e: InvalidTokenException) {
                signOut()
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
                }.toCardEntity()
                _fetchedCard.value = fetched
            } catch (e: InvalidTokenException) {
                signOut()
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
                val cards = _cards.value.toMutableList()
                cards.removeIf { it.cardId == cardId }
                _cards.value = cards
                if (_selectedCardId.value == cardId) {
                    _selectedCardId.value = null
                }
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete card: ${e.localizedMessage}"
                Log.e(tag, "Failed to delete card", e)
            } finally {
                _isCardDeleting.value = false
            }
        }
    }

    fun resetCard(cardId: String) {
        viewModelScope.launch {
            Log.d(tag, "reset card $cardId")
            _isCardResetting.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    cardsRepository.resetCard(cardId)
                }
                val cards = _cards.value.toMutableList()
                cards.toList().forEachIndexed { index, card ->
                    if (card.cardId == cardId) {
                        cards[index] = card.copy(answered = 0)
                        return@forEachIndexed
                    }
                }
                _cards.value = cards
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete card: ${e.localizedMessage}"
                Log.e(tag, "Failed to delete card", e)
            } finally {
                _isCardResetting.value = false
            }
        }
    }

    fun loadNextCardDeck(
        dictionaryIds: Set<String>,
        random: Boolean,
        unknown: Boolean,
        length: Int,
    ) {
        viewModelScope.launch {
            _isCardsDeckLoading.value = true
            _stageShowCurrentDeckCardIndex.intValue = 0
            _errorMessage.value = null
            _cardsDeck.value = emptyList()
            _wrongAnsweredCardDeckIds.value = emptySet()
            try {
                val cards = withContext(Dispatchers.IO) {
                    cardsRepository.getCardsDeck(
                        dictionaryIds = dictionaryIds.toList(),
                        random = random,
                        unknown = unknown,
                        length = length,
                    )
                }.map { it.toCardEntity() }
                if (cards.isEmpty()) {
                    _errorMessage.value = "No cards available in the selected dictionaries."
                }
                _cardsDeck.value = cards
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards deck: ${e.localizedMessage}"
                Log.e(tag, "Failed to load cards deck", e)
            } finally {
                _isCardsDeckLoading.value = false
            }
        }
    }

    fun loadAndPlayAudio(card: CardEntity) {
        val cardId = checkNotNull(card.cardId)
        if (isAudioPlaying(cardId)) {
            Log.d(
                tag,
                "loadAndPlayAudion: audio is already playing for [cardId = $cardId (${card.word})]"
            )
            return
        }
        if (isAudioLoaded(cardId)) {
            playAudio(card)
        } else {
            loadAudio(cardId, card.audioId) {
                playAudio(card)
            }
        }
    }

    fun waitForAudioToFinish(cardId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            while (isAudioOperationInProgress(cardId)) {
                delay(100)
            }
            onComplete()
        }
    }

    private fun loadAudio(cardId: String, audioResourceId: String, onLoaded: () -> Unit) {
        if (_audioResources.get(cardId) != null) {
            if (_audioResources.getNullable(cardId) != null) {
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
        _isAudioLoading[cardId] = true
        _errorMessage.value = null
        val lang = audioResourceId.substringBefore(":")
        val word = audioResourceId.substringAfter(":")
        try {
            val resource = withContext(Dispatchers.IO) {
                ttsRepository.get(lang, word)
            }
            _audioResources.putNullable(cardId, resource)
            if (resource != null) {
                onLoaded()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load audio: ${e.localizedMessage}"
            Log.e(tag, "Failed to load audio", e)
        } finally {
            _isAudioLoading.remove(cardId)
        }
    }

    private fun playAudio(card: CardEntity) {
        val cardId = checkNotNull(card.cardId)

        val audioData = _audioResources.get(cardId)
        @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
        if (audioData == null || audioData == NULL_AUDIO_MARKER) {
            Log.d(tag, "playAudion: no audio data for [cardId = $cardId (${card.word})]")
            return
        }

        if (_isAudioPlaying.putIfAbsent(cardId, true) == true) {
            Log.d(tag, "playAudion: audio is already playing for [cardId = $cardId (${card.word})]")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            var tempFile: Path? = null
            try {
                val tmpFileName = card.audioId
                    .replace(":", "-")
                    .replace(" ", "")
                    .replace(",", "-")
                tempFile =
                    Files.createTempFile("temp-audio-$tmpFileName-", ".mp3")
                Log.d(
                    tag,
                    "playAudion: create temp file $tempFile for [cardId = $cardId (${card.word})]"
                )
                tempFile.outputStream().use {
                    it.write(audioData)
                }

                withContext(Dispatchers.Main) {
                    var isCompleted = false
                    MediaPlayer().apply {
                        val timeoutJob = viewModelScope.launch {
                            delay(PLAY_AUDIO_EMERGENCY_TIMEOUT_MS)
                            if (!isCompleted && !isPlaying) {
                                Log.w(tag, "playAudion: timeout reached. Stopping playback.")
                                releaseAudioResources(cardId, tempFile)
                            }
                        }
                        setDataSource(tempFile.toFile().absolutePath)
                        setOnPreparedListener {
                            try {
                                if (!isPlaying) {
                                    Log.d(
                                        tag,
                                        "playAudio: starting playback [cardId = $cardId (${card.word})]"
                                    )
                                    start()
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    tag,
                                    "playAudion: Error during playback start [cardId = $cardId (${card.word})]:" +
                                            " ${e.localizedMessage}",
                                    e
                                )
                                releaseAudioResources(cardId, tempFile)
                            }
                        }
                        setOnCompletionListener {
                            try {
                                Log.d(
                                    tag,
                                    "playAudion: completed [cardId = $cardId (${card.word})]"
                                )
                                onFinish()
                                isCompleted = true
                            } finally {
                                releaseAudioResources(cardId, tempFile)
                                timeoutJob.cancel()
                            }
                        }
                        setOnErrorListener { mp, _, _ ->
                            try {
                                Log.d(tag, "playAudion: error, [cardId = $cardId (${card.word})]")
                                mp.onFinish()
                                isCompleted = true
                                true
                            } finally {
                                releaseAudioResources(cardId, tempFile)
                                timeoutJob.cancel()
                            }
                        }
                        prepareAsync()
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    tag,
                    "playAudion: failed [cardId = $cardId (${card.word})]: ${e.localizedMessage}",
                    e
                )
                releaseAudioResources(cardId, tempFile)
            }
        }
    }

    private fun releaseAudioResources(cardId: String, tempFile: Path?) {
        viewModelScope.launch(Dispatchers.Main) {
            _isAudioPlaying.remove(cardId)
            tempFile?.deleteIfExists()
        }
    }

    private fun MediaPlayer.onFinish() {
        stop()
        reset()
        release()
    }

    private fun isAudioLoaded(cardId: String): Boolean {
        return _audioResources.get(cardId) != null
    }

    @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
    fun audioIsAvailable(cardId: String): Boolean {
        return _audioResources.get(cardId) != NULL_AUDIO_MARKER
    }

    fun isAudioLoading(cardId: String): Boolean {
        return _isAudioLoading[cardId] ?: false
    }

    fun isAudioOperationInProgress(cardId: String): Boolean {
        return isAudioLoading(cardId) || isAudioPlaying(cardId)
    }

    private fun isAudioPlaying(cardId: String): Boolean {
        return _isAudioPlaying[cardId] ?: false
    }

    fun selectCard(cardId: String?) {
        _selectedCardId.value = cardId
    }

    fun clearFetchedCard() {
        _fetchedCard.value = null
    }

    fun nextDeckCard(numberOfRightAnswers: Int, onNextCard: (CardEntity) -> Unit = {}): Boolean {
        val currentDeck = _cardsDeck.value
        var nextIndex = _stageShowCurrentDeckCardIndex.intValue + 1

        while (nextIndex < currentDeck.size && currentDeck[nextIndex].answered >= numberOfRightAnswers) {
            nextIndex++
        }

        return if (nextIndex < currentDeck.size) {
            _stageShowCurrentDeckCardIndex.intValue = nextIndex
            onNextCard(currentDeck[nextIndex])
            true
        } else {
            false
        }
    }

    fun markShowStageCurrentDeckCardAsAnsweredCorrectly(
        numberOfRightAnswers: Int,
        onResultStage: () -> Unit
    ) {
        val currentDeck = _cardsDeck.value.toMutableList()
        val currentIndex = _stageShowCurrentDeckCardIndex.intValue

        if (currentIndex in currentDeck.indices) {
            val currentCard = currentDeck[currentIndex]
            val newCard = currentCard.copy(answered = numberOfRightAnswers)
            currentDeck[currentIndex] = newCard
            _cardsDeck.value = currentDeck
            updateCard(newCard)
        }

        if (allDeckCardsAnsweredCorrectly(numberOfRightAnswers)) {
            onResultStage()
        } else if (!nextDeckCard(numberOfRightAnswers)) {
            onResultStage()
        }
    }

    fun updateDeckCard(
        cardId: String,
        numberOfRightAnswers: Int,
    ) {
        var card = checkNotNull(_cardsDeck.value.singleOrNull { it.cardId == cardId }) {
            "Can't find deck card = $cardId"
        }
        card = if (!_wrongAnsweredCardDeckIds.value.contains(cardId)) {
            card.copy(answered = card.answered + 1)
        } else if (card.answered >= numberOfRightAnswers) {
            card.copy(answered = numberOfRightAnswers - 1)
        } else {
            return
        }
        updateCard(card)
        val cardsDeck = _cardsDeck.value.toMutableList()
        val index = cardsDeck.indexOfFirst { it.cardId == cardId }
        cardsDeck[index] = card
        _cardsDeck.value = cardsDeck
    }

    fun markDeckCardAsWrong(cardId: String) {
        val ids = _wrongAnsweredCardDeckIds.value.toMutableSet()
        ids.add(cardId)
        _wrongAnsweredCardDeckIds.value = ids
    }

    fun greenDeckCards(numberOfRightAnswers: (CardEntity) -> Int): List<CardEntity> {
        return _cardsDeck.value
            .filter { it.answered >= numberOfRightAnswers(it) }
            .filter {
                !_wrongAnsweredCardDeckIds.value.contains(it.cardId)
            }.sortedBy { -it.answered }
    }

    fun blueDeckCards(numberOfRightAnswers: (CardEntity) -> Int): List<CardEntity> {
        return _cardsDeck.value
            .filter { it.answered < numberOfRightAnswers(it) }
            .filter {
                !_wrongAnsweredCardDeckIds.value.contains(it.cardId)
            }.sortedBy { -it.answered }
    }

    fun redDeckCards(): List<CardEntity> {
        return _cardsDeck.value
            .filter {
                _wrongAnsweredCardDeckIds.value.contains(it.cardId)
            }.sortedBy { -it.answered }
    }

    private fun allDeckCardsAnsweredCorrectly(numberOfRightAnswers: Int): Boolean =
        _wrongAnsweredCardDeckIds.value.isEmpty() && _cardsDeck.value.all { it.answered >= numberOfRightAnswers }

    fun allDeckCardsAnsweredCorrectly(numberOfRightAnswers: (CardEntity) -> Int): Boolean =
        _wrongAnsweredCardDeckIds.value.isEmpty() && _cardsDeck.value.all {
            it.answered >= numberOfRightAnswers(
                it
            )
        }

    fun numberOfKnownCards(numberOfRightAnswers: Int): Int =
        _cards.value.count { it.answered >= numberOfRightAnswers }

    fun unknownDeckCards(selectNumberOfRightAnswers: (dictionaryId: String) -> Int): List<CardEntity> =
        _cardsDeck.value.filter {
            it.answered < selectNumberOfRightAnswers(checkNotNull(it.dictionaryId) {
                "Null dictionaryId for card $it"
            })
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