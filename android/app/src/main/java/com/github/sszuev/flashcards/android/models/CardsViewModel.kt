package com.github.sszuev.flashcards.android.models

import android.util.Log
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                _errorMessage.value = "Failed to load cards. Press HOME to refresh the page."
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
                        cards[index] = card
                        return@forEachIndexed
                    }
                }
                _cards.value = cards
            } catch (e: InvalidTokenException) {
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
                _errorMessage.value = "Failed to create card. Press HOME to refresh the page."
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
                _errorMessage.value =
                    "Failed to fetch card data for word '$word'. Press HOME to refresh the page."
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
                _errorMessage.value = "Failed to delete card. Press HOME to refresh the page."
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
        _fetchedCard.value = null
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