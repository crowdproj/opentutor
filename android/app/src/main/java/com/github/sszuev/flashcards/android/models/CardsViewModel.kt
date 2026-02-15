package com.github.sszuev.flashcards.android.models

import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.repositories.InvalidTokenException
import com.github.sszuev.flashcards.android.repositories.TranslationRepository
import com.github.sszuev.flashcards.android.toCardEntity
import com.github.sszuev.flashcards.android.toCardResource
import com.github.sszuev.flashcards.android.utils.translationAsString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class CardsViewModel(
    private val cardsRepository: CardsRepository,
    private val translationRepository: TranslationRepository,
    private val signOut: () -> Unit,
) : ViewModel() {

    private val tag = "CardsViewModel"

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
    private val _isCardUpdating = mutableStateOf(true)
    private val _isCardFetching = mutableStateOf(true)
    val isCardFetching: State<Boolean> get() = _isCardFetching
    private val _fetchedCard = mutableStateOf<CardEntity?>(null)
    val fetchedCard: State<CardEntity?> get() = _fetchedCard
    private val _isCardCreating = mutableStateOf(true)
    private val _isCardDeleting = mutableStateOf(true)
    private val _isCardResetting = mutableStateOf(true)

    private val fetchedCardsCache = LruCache<Triple<String, String, String>, CardEntity>(1024)

    // last-click-wins:
    private var fetchJob: Job? = null
    private var fetchGen: Long = 0
    private val _activeFetchKey = mutableStateOf<Triple<String, String, String>?>(null)
    val activeFetchKey: State<Triple<String, String, String>?> get() = _activeFetchKey
    private val _fetchedCardKey = mutableStateOf<Triple<String, String, String>?>(null)
    val fetchedCardKey: State<Triple<String, String, String>?> get() = _fetchedCardKey

    val selectedCard: CardEntity?
        get() = if (_selectedCardId.value == null) null else {
            _cards.value.singleOrNull { it.cardId == _selectedCardId.value }
        }

    fun loadCards(dictionaryId: String) {
        viewModelScope.launch {
            Log.i(tag, "load cards for dictionary = $dictionaryId")
            _isCardsLoading.value = true
            _errorMessage.value = null
            _cards.value = emptyList()
            try {
                _cards.value = withContext(Dispatchers.IO) {
                    cardsRepository
                        .getAll(dictionaryId)
                        .map { it.toCardEntity() }
                        .sortedBy { it.word }
                }
                _selectedCardId.value = null
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards. Press HOME to refresh the page."
                Log.e(tag, "Failed to load cards", e)
            } finally {
                _isCardsLoading.value = false
            }
        }
    }

    fun updateCard(card: CardEntity) {
        viewModelScope.launch {
            Log.i(tag, "Update card with id = ${card.cardId}")
            _isCardUpdating.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    cardsRepository.updateCard(card.toCardResource())
                }
                val cards = _cards.value.toMutableList()
                _cards.value.forEachIndexed { index, entity ->
                    if (entity.cardId == card.cardId) {
                        cards[index] = card
                        return@forEachIndexed
                    }
                }
                _cards.value = cards
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update card. Press HOME to refresh the page."
                Log.e(tag, "Failed to load cards", e)
            } finally {
                _isCardUpdating.value = false
            }
        }
    }

    fun createCard(card: CardEntity) {
        viewModelScope.launch {
            Log.i(tag, "create card")
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
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create card. Press HOME to refresh the page."
                Log.e(tag, "Failed to create card", e)
            } finally {
                _isCardCreating.value = false
            }
        }
    }

    fun fetchCard(word: String, sourceLang: String, targetLang: String) {
        val w = word.trim()
        if (w.isBlank()) {
            clearFetchedCard()
            return
        }

        val key = Triple(w, sourceLang, targetLang)

        _activeFetchKey.value = key
        _errorMessage.value = null

        _fetchedCard.value = null
        _fetchedCardKey.value = null

        // last-click-wins
        val myGen = ++fetchGen

        fetchJob?.cancel()
        fetchJob = null

        // cache hit — мгновенно отдаём
        fetchedCardsCache.get(key)?.let { cached ->
            _fetchedCard.value = cached
            _fetchedCardKey.value = key
            _isCardFetching.value = false
            return
        }

        _isCardFetching.value = true

        fetchJob = viewModelScope.launch {
            Log.i(tag, "Fetch card data ['$w'; $sourceLang -> $targetLang]")
            try {
                val fetched = withContext(Dispatchers.IO) {
                    translationRepository.fetch(
                        query = w,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                    )
                }.toCardEntity()

                if (myGen != fetchGen) return@launch
                if (_activeFetchKey.value != key) return@launch

                _fetchedCard.value = fetched
                _fetchedCardKey.value = key
                fetchedCardsCache.put(key, fetched)
            } catch (_: CancellationException) {
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                if (myGen == fetchGen && _activeFetchKey.value == key) {
                    _fetchedCard.value = null
                    _fetchedCardKey.value = key
                    _errorMessage.value =
                        "Failed to fetch card data for word '$w'. Press HOME to refresh the page."
                }
                Log.e(tag, "Failed to fetch card data", e)
            } finally {
                if (myGen == fetchGen && _activeFetchKey.value == key) {
                    _isCardFetching.value = false
                }
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            Log.i(tag, "delete card")
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
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete card. Press HOME to refresh the page."
                Log.e(tag, "Failed to delete card", e)
            } finally {
                _isCardDeleting.value = false
            }
        }
    }

    fun resetCard(cardId: String) {
        viewModelScope.launch {
            Log.i(tag, "reset card $cardId")
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
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete card. Press HOME to refresh the page."
                Log.e(tag, "Failed to delete card", e)
            } finally {
                _isCardResetting.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectCard(cardId: String?) {
        _selectedCardId.value = cardId
    }

    fun clearFetchedCard() {
        fetchJob?.cancel()
        fetchJob = null

        fetchGen++
        _activeFetchKey.value = null
        _fetchedCardKey.value = null
        _fetchedCard.value = null
        _isCardFetching.value = false
    }

    fun numberOfKnownCards(numberOfRightAnswers: Int): Int =
        _cards.value.count { it.answered >= numberOfRightAnswers }

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
                "translation" -> a.translationAsString.compareTo(b.translationAsString)
                "status" -> a.answered.compareTo(b.answered)
                else -> 0
            }
            if (isAscending.value) result else -result
        }
    }
}