package com.github.sszuev.flashcards.android.models

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
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

class CardViewModel(
    private val cardsRepository: CardsRepository,
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
    private val _cardsDeck = mutableStateOf<List<CardEntity>>(emptyList())
    val cardsDeck: State<List<CardEntity>> = _cardsDeck
    private val _wrongAnsweredCardDeckIds = mutableStateOf<Set<String>>(emptySet())
    val wrongAnsweredCardDeckIds: State<Set<String>> = _wrongAnsweredCardDeckIds
    private val _answeredCardDeckIds = mutableStateOf<Set<String>>(emptySet())
    private val _isAdditionalCardsDeckLoading = mutableStateOf(true)
    val isAdditionalCardsDeckLoading: State<Boolean> = _isAdditionalCardsDeckLoading
    private val _additionalCardsDeck = mutableStateOf<List<CardEntity>>(emptyList())
    val additionalCardsDeck: State<List<CardEntity>> = _additionalCardsDeck

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
                        cards[index] = card
                        return@forEachIndexed
                    }
                }
                _cards.value = cards
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update card: ${e.localizedMessage}"
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
                _errorMessage.value = "Failed to fetch card data for word '$word': ${e.localizedMessage}"
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
        length: Int,
        onComplete: (cards: List<CardEntity>) -> Unit,
    ) {
        viewModelScope.launch {
            _isCardsDeckLoading.value = true
            _stageShowCurrentDeckCardIndex.intValue = 0
            _errorMessage.value = null
            _cardsDeck.value = emptyList()
            _wrongAnsweredCardDeckIds.value = emptySet()
            _answeredCardDeckIds.value = emptySet()
            try {
                val cards = withContext(Dispatchers.IO) {
                    cardsRepository.getCardsDeck(
                        dictionaryIds = dictionaryIds.toList(),
                        random = true,
                        unknown = true,
                        length = length,
                    ).distinct()
                }.map { it.toCardEntity() }
                if (cards.isEmpty()) {
                    _errorMessage.value = "No cards available in the selected dictionaries."
                }
                onComplete(cards)
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

    fun loadAdditionalCardDeck(
        dictionaryIds: Set<String>,
        length: Int,
    ) {
        viewModelScope.launch {
            _isAdditionalCardsDeckLoading.value = true
            _errorMessage.value = null
            _additionalCardsDeck.value = emptyList()
            try {
                val cards = withContext(Dispatchers.IO) {
                    cardsRepository.getCardsDeck(
                        dictionaryIds = dictionaryIds.toList(),
                        random = true,
                        unknown = false,
                        length = length,
                    ).distinct()
                }.map { it.toCardEntity() }
                if (cards.isEmpty()) {
                    _errorMessage.value = "No cards available in the selected dictionaries."
                }
                _additionalCardsDeck.value = cards
            } catch (e: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards deck: ${e.localizedMessage}"
                Log.e(tag, "Failed to load cards deck", e)
            } finally {
                _isAdditionalCardsDeckLoading.value = false
            }
        }
    }

    fun selectCard(cardId: String?) {
        _selectedCardId.value = cardId
    }

    fun clearFetchedCard() {
        _fetchedCard.value = null
    }

    fun markDeckCardAsKnow(
        cardId: String,
        numberOfRightAnswers: Int,
    ) {
        var card = checkNotNull(_cardsDeck.value.singleOrNull { it.cardId == cardId }) {
            "Can't find deck card = $cardId"
        }
        val answered = _answeredCardDeckIds.value.toMutableSet()
        answered.add(cardId)
        _answeredCardDeckIds.value = answered
        card = card.copy(answered = numberOfRightAnswers)
        updateCard(card)
        val cardsDeck = _cardsDeck.value.toMutableList()
        val index = cardsDeck.indexOfFirst { it.cardId == cardId }
        cardsDeck[index] = card
        _cardsDeck.value = cardsDeck
    }

    fun updateDeckCard(
        cardId: String,
        numberOfRightAnswers: Int,
    ) {
        var card = checkNotNull(_cardsDeck.value.singleOrNull { it.cardId == cardId }) {
            "Can't find deck card = $cardId"
        }
        val answered = _answeredCardDeckIds.value.toMutableSet()
        answered.add(cardId)
        _answeredCardDeckIds.value = answered
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
        val answered = _answeredCardDeckIds.value.toMutableSet()
        answered.add(cardId)
        _answeredCardDeckIds.value = answered
        val ids = _wrongAnsweredCardDeckIds.value.toMutableSet()
        ids.add(cardId)
        _wrongAnsweredCardDeckIds.value = ids
    }

    fun greenDeckCards(numberOfRightAnswers: (CardEntity) -> Int): List<CardEntity> {
        return _cardsDeck.value
            .filter { _answeredCardDeckIds.value.contains(it.cardId) }
            .filter { it.answered >= numberOfRightAnswers(it) }
            .filter {
                !_wrongAnsweredCardDeckIds.value.contains(it.cardId)
            }.sortedBy { -it.answered }
    }

    fun blueDeckCards(numberOfRightAnswers: (CardEntity) -> Int): List<CardEntity> {
        return _cardsDeck.value
            .filter { _answeredCardDeckIds.value.contains(it.cardId) }
            .filter { it.answered < numberOfRightAnswers(it) }
            .filter {
                !_wrongAnsweredCardDeckIds.value.contains(it.cardId)
            }.sortedBy { -it.answered }
    }

    fun redDeckCards(): List<CardEntity> {
        return _cardsDeck.value
            .filter { _answeredCardDeckIds.value.contains(it.cardId) }
            .filter {
                _wrongAnsweredCardDeckIds.value.contains(it.cardId)
            }.sortedBy { -it.answered }
    }

    fun numberOfKnownCards(numberOfRightAnswers: Int): Int =
        _cards.value.count { it.answered >= numberOfRightAnswers }

    fun unknownDeckCards(selectNumberOfRightAnswers: (dictionaryId: String) -> Int): List<CardEntity> =
        _cardsDeck.value.filter {
            it.answered < selectNumberOfRightAnswers(checkNotNull(it.dictionaryId) {
                "Null dictionaryId for card $it"
            })
        }.shuffled()

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