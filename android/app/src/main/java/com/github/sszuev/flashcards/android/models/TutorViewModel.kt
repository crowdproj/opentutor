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
import com.github.sszuev.flashcards.android.toCardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TutorViewModel(
    private val cardsRepository: CardsRepository,
    private val signOut: () -> Unit,
) : ViewModel() {

    private val tag = "TutorViewModel"

    private val _cards = mutableStateOf<List<CardEntity>>(emptyList())
    val cards: State<List<CardEntity>> = _cards
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isCardsDeckLoading = mutableStateOf(true)
    val isCardsDeckLoading: State<Boolean> get() = _isCardsDeckLoading
    private val _stageShowCurrentDeckCardIndex = mutableIntStateOf(0)
    private val _cardsDeck = mutableStateOf<List<CardEntity>>(emptyList())
    val cardsDeck: State<List<CardEntity>> = _cardsDeck
    private val _wrongAnsweredCardDeckIds = mutableStateOf<Set<String>>(emptySet())
    val wrongAnsweredCardDeckIds: State<Set<String>> = _wrongAnsweredCardDeckIds
    private val _answeredCardDeckIds = mutableStateOf<Set<String>>(emptySet())
    private val _isAdditionalCardsDeckLoading = mutableStateOf(false)
    val isAdditionalCardsDeckLoading: State<Boolean> = _isAdditionalCardsDeckLoading
    private val _additionalCardsDeck = mutableStateOf<List<CardEntity>>(emptyList())
    val additionalCardsDeck: State<List<CardEntity>> = _additionalCardsDeck

    val stageMosaicSelectedLeftCardId = mutableStateOf<String?>(null)
    val stageMosaicSelectedRightCardId = mutableStateOf<String?>(null)
    val stageMosaicLeftCards = mutableStateOf<List<CardEntity>>(emptyList())
    val stageMosaicRightCards = mutableStateOf<List<CardEntity>>(emptyList())
    private val _isStageMosaicInitialized = mutableStateOf(false)

    val stageOptionsLeftCards = mutableStateOf<List<CardEntity>>(emptyList())
    val stageOptionsCardsMap = mutableStateOf<Map<CardEntity, List<CardEntity>>>(emptyMap())
    val stageOptionsCurrentCard = mutableStateOf<CardEntity?>(null)
    val stageOptionsSelectedOption = mutableStateOf<CardEntity?>(null)
    val stageOptionsIsCorrect = mutableStateOf<Boolean?>(null)
    private val _isStageOptionsInitialized = mutableStateOf(false)

    private val _isAdditionalDeckLoaded = mutableStateOf(false)
    val isAdditionalDeckLoaded: Boolean get() = _isAdditionalDeckLoaded.value

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
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards deck. Press HOME to refresh the page."
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
            Log.d(tag, "load additional card deck with length = $length")
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
                _isAdditionalDeckLoaded.value = true
            } catch (_: InvalidTokenException) {
                signOut()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cards deck. Press HOME to refresh the page."
                Log.e(tag, "Failed to load additional cards deck", e)
            } finally {
                _isAdditionalCardsDeckLoading.value = false
            }
        }
    }

    fun initStageMosaic(
        selectNumberOfRightAnswers: (dictionaryId: String) -> Int,
        numberOfWords: Int
    ) {
        if (_isStageMosaicInitialized.value) {
            return
        }

        val left = unknownDeckCards(selectNumberOfRightAnswers).shuffled().take(numberOfWords)
        val right = cardsDeck.value.shuffled()

        stageMosaicLeftCards.value = left
        stageMosaicRightCards.value = right
        stageMosaicSelectedLeftCardId.value = null
        stageMosaicSelectedRightCardId.value = null

        _isStageMosaicInitialized.value = true
    }

    fun initStageOptions(
        selectNumberOfRightAnswers: (dictionaryId: String) -> Int,
        numberOfWordsPerStage: Int,
    ) {
        if (_isStageOptionsInitialized.value) {
            return
        }
        val cards =
            unknownDeckCards(selectNumberOfRightAnswers).shuffled().take(numberOfWordsPerStage)

        stageOptionsLeftCards.value = cards
        stageOptionsCurrentCard.value = cards.firstOrNull()

        _isStageOptionsInitialized.value = true
    }

    fun generateOptionsCardsMap(numberOfVariants: Int) {
        val leftCards = stageOptionsLeftCards.value
        val rightCards = additionalCardsDeck.value
        if (stageOptionsCardsMap.value.isNotEmpty()) {
            Log.d(
                tag,
                "Options map has already been initialized: map=${stageOptionsCardsMap.value.size}, " +
                        "left=${leftCards.size}, right=${rightCards.size}"
            )
            return
        }
        if (leftCards.isEmpty() || rightCards.size < leftCards.size) {
            Log.w(
                tag, "Can't generate options map: map=${stageOptionsCardsMap.value.size}, " +
                        "left=${leftCards.size}, right=${rightCards.size}"
            )
            return
        }

        val map = leftCards.associateWith { leftCard ->
            val variants = (rightCards.shuffled()
                .take(numberOfVariants - 1) + leftCard)
                .distinctBy { it.cardId }.shuffled()
            variants
        }
        stageOptionsCardsMap.value = map
    }

    fun clearMosaicAndOptionsState() {
        stageMosaicLeftCards.value = emptyList()
        stageMosaicRightCards.value = emptyList()
        stageMosaicSelectedLeftCardId.value = null
        stageMosaicSelectedRightCardId.value = null
        _isStageMosaicInitialized.value = false

        stageOptionsLeftCards.value = emptyList()
        stageOptionsCardsMap.value = emptyMap()
        stageOptionsCurrentCard.value = null
        _isStageOptionsInitialized.value = false

        _isAdditionalDeckLoaded.value = false
    }

    fun resetSession() {
        _cardsDeck.value = emptyList()
        _additionalCardsDeck.value = emptyList()
        _answeredCardDeckIds.value = emptySet()
        _wrongAnsweredCardDeckIds.value = emptySet()
        _stageShowCurrentDeckCardIndex.intValue = 0
        _isCardsDeckLoading.value = true
        _isAdditionalCardsDeckLoading.value = false
        _isAdditionalDeckLoaded.value = false

        // Stage Mosaic
        stageMosaicLeftCards.value = emptyList()
        stageMosaicRightCards.value = emptyList()
        stageMosaicSelectedLeftCardId.value = null
        stageMosaicSelectedRightCardId.value = null
        _isStageMosaicInitialized.value = false

        // Stage Options
        stageOptionsLeftCards.value = emptyList()
        stageOptionsCardsMap.value = emptyMap()
        stageOptionsCurrentCard.value = null
        stageOptionsSelectedOption.value = null
        stageOptionsIsCorrect.value = null
        _isStageOptionsInitialized.value = false
    }

    fun markDeckCardAsKnow(
        cardId: String,
        numberOfRightAnswers: Int,
        updateCard: (CardEntity) -> Unit,
    ) {
        var card = checkNotNull(_cardsDeck.value.singleOrNull { it.cardId == cardId }) {
            "Can't find deck card = $cardId"
        }
        Log.d(tag, "Mark card '${card.word}' ($cardId) as wrong.")
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
        wrong: Boolean,
        updateCard: (CardEntity) -> Unit,
    ) {
        val card = checkNotNull(_cardsDeck.value.singleOrNull { it.cardId == cardId }) {
            "Can't find deck card = $cardId"
        }

        val answered = _answeredCardDeckIds.value.toMutableSet()
        answered.add(cardId)
        _answeredCardDeckIds.value = answered

        if (wrong) {
            Log.d(tag, "Mark card '${card.word}' ($cardId) as wrong")
            val ids = _wrongAnsweredCardDeckIds.value.toMutableSet()
            ids.add(cardId)
            _wrongAnsweredCardDeckIds.value = ids
        }
        val newCard = if (_wrongAnsweredCardDeckIds.value.contains(cardId)) {
            if (card.answered >= numberOfRightAnswers) {
                card.copy(answered = numberOfRightAnswers - 1)
            } else {
                card
            }
        } else {
            if (card.answered >= numberOfRightAnswers - 1) {
                card.copy(answered = numberOfRightAnswers)
            } else {
                card.copy(answered = card.answered + 1)
            }
        }
        if (newCard.answered != card.answered) {
            Log.d(
                tag, "Update card '${card.word}' ($cardId) => " +
                        "answered = ${newCard.answered} (wrong = $wrong)"
            )
            updateCard(newCard)
            val cardsDeck = _cardsDeck.value.toMutableList()
            val index = cardsDeck.indexOfFirst { it.cardId == cardId }
            cardsDeck[index] = newCard
            _cardsDeck.value = cardsDeck
        }
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

    fun unknownDeckCards(selectNumberOfRightAnswers: (dictionaryId: String) -> Int): List<CardEntity> =
        _cardsDeck.value.filter {
            it.answered < selectNumberOfRightAnswers(checkNotNull(it.dictionaryId) {
                "Null dictionaryId for card $it"
            })
        }.shuffled()
}