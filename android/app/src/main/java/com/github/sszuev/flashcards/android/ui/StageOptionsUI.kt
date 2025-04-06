package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.STAGE_OPTIONS_CELL_DELAY_MS
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "StageOptionsUI"

@Composable
fun StageOptionsScreen(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageOptions")
    if (tutorViewModel.cardsDeck.value.isEmpty()) {
        onNextStage()
        return
    }
    BackHandler {
        onHomeClick()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Stage: options [${if (direction) "source -> target" else "target -> source"}]",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OptionsPanelDirect(
                tutorViewModel = tutorViewModel,
                dictionariesViewModel = dictionariesViewModel,
                cardsViewModel = cardsViewModel,
                settingsViewModel = settingsViewModel,
                ttsViewModel = ttsViewModel,
                onNextStage = onNextStage,
                direct = direction,
            )
        }
    }
}

@Composable
fun OptionsPanelDirect(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onNextStage: () -> Unit,
    direct: Boolean,
) {
    val hasNavigated = remember { mutableStateOf(false) }

    if (hasNavigated.value) {
        return
    }

    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val leftCards = remember {
        tutorViewModel.unknownDeckCards { id ->
            dictionariesViewModel.dictionaryById(id).numberOfRightAnswers
        }.shuffled().take(settings.numberOfWordsPerStage).also { cards ->
            cards.firstOrNull()?.let {
                if (direct) {
                    tutorViewModel.viewModelScope.launch {
                        Log.d(
                            tag,
                            "direct: playing audio for: [${it.cardId}: ${it.word}]"
                        )
                        ttsViewModel.loadAndPlayAudio(it)
                        ttsViewModel.waitForAudionProcessing(checkNotNull(it.cardId))
                    }
                }
            }
        }
    }

    if (leftCards.isEmpty()) {
        onNextStage()
        return
    }

    val rightCardsSize = leftCards.size * (settings.stageOptionsNumberOfVariants - 1)
    LaunchedEffect(Unit) {
        tutorViewModel.loadAdditionalCardDeck(
            dictionaryIds = dictionariesViewModel.selectedDictionaryIds.value,
            length = rightCardsSize
        )
    }
    if (tutorViewModel.isAdditionalCardsDeckLoading.value) {
        CircularProgressIndicator()
        return
    }

    val errorMessage = tutorViewModel.errorMessage.value
    ErrorMessageBox(errorMessage)
    if (errorMessage != null) {
        return
    }

    if (tutorViewModel.additionalCardsDeck.value.size <= 1) {
        Log.d(tag, "onNextStage: not enough additional cards")
        onNextStage()
        return
    }

    val cardsMap = remember { mutableStateOf<Map<CardEntity, List<CardEntity>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val newMap = leftCards.associateWith { leftCard ->
            val rightCards = tutorViewModel.additionalCardsDeck.value.shuffled()
                .take(settings.stageOptionsNumberOfVariants - 1)
            (rightCards + leftCard).distinctBy { it.cardId }.shuffled()
        }
        Log.d(tag, "=".repeat(42))
        newMap.forEach { (card, options) ->
            Log.d(tag, "${card.cardId} => ${options.map { it.cardId }}")
        }
        cardsMap.value = newMap
    }

    val currentCard = remember { mutableStateOf(leftCards.firstOrNull()) }
    val selectedOption = remember { mutableStateOf<CardEntity?>(null) }
    val isCorrect = remember { mutableStateOf<Boolean?>(null) }

    val isAnswerProcessing = remember { mutableStateOf(false) }

    fun color(isSelected: Boolean, isCorrect: Boolean?): Color = when {
        isSelected && isCorrect == true ->
            Color.Green

        isCorrect == false ->
            Color.Red

        isSelected ->
            Color.Blue

        else -> Color.Gray
    }

    fun match(selectedItem: CardEntity): Boolean {
        return selectedItem.cardId == currentCard.value?.cardId
    }

    fun resetState() {
        currentCard.value = null
        cardsMap.value = emptyMap()
        hasNavigated.value = true
    }

    fun onOptionSelected(selectedItem: CardEntity) {
        tutorViewModel.viewModelScope.launch {
            if (isAnswerProcessing.value) {
                Log.d(tag, "Click ignored: Answer is being processed!")
                return@launch
            }
            isAnswerProcessing.value = true
            try {
                selectedOption.value = selectedItem
                isCorrect.value = match(selectedItem)

                if (!direct) {
                    Log.d(
                        tag,
                        "reverse: playing audio for: [${selectedItem.cardId}: ${selectedItem.word}]"
                    )
                    ttsViewModel.loadAndPlayAudio(selectedItem)
                    ttsViewModel.waitForAudionProcessing(checkNotNull(selectedItem.cardId))
                } else {
                    delay(STAGE_OPTIONS_CELL_DELAY_MS)
                }

                if (isCorrect.value == true) {
                    currentCard.value?.let { cardsMap.value -= it }

                    if (cardsMap.value.isEmpty()) {
                        resetState()
                        Log.d(tag, "onNextStage: no more cards to proceed")
                        onNextStage()
                        return@launch
                    }

                    val nextCard = cardsMap.value.keys.firstOrNull()

                    if (nextCard == null) {
                        resetState()
                        Log.d(tag, "onNextStage: null next card")
                        onNextStage()
                        return@launch
                    }

                    selectedOption.value = null
                    isCorrect.value = null

                    currentCard.value = nextCard

                    val cardId = checkNotNull(nextCard.cardId)
                    val dictionaryId = checkNotNull(nextCard.dictionaryId)
                    val dictionary = dictionariesViewModel.dictionaryById(dictionaryId)

                    tutorViewModel.updateDeckCard(
                        cardId = cardId,
                        numberOfRightAnswers = dictionary.numberOfRightAnswers,
                        updateCard = { cardsViewModel.updateCard(it) }
                    )
                    if (direct) {
                        Log.d(
                            tag,
                            "direct: playing audio for: [${cardId}: ${nextCard.word}]"
                        )
                        ttsViewModel.loadAndPlayAudio(nextCard)
                        ttsViewModel.waitForAudionProcessing(cardId)
                    }
                } else {
                    val cardId = checkNotNull(currentCard.value?.cardId)
                    Log.i(tag, "Wrong answer for card $cardId")
                    selectedOption.value = null
                    isCorrect.value = null
                    tutorViewModel.markDeckCardAsWrong(cardId)
                }
                selectedOption.value = null
                isCorrect.value = null
            } finally {
                isAnswerProcessing.value = false
            }
        }
    }

    Log.d(tag, "current word: [${currentCard.value?.cardId}: ${currentCard.value?.word}]")

    val card = currentCard.value
    if (card == null) {
        Log.d(tag, "onNextStage: currentCard is null")
        resetState()
        onNextStage()
        return
    }

    val dictionary =
        dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (direct || isTextShort(card.translationAsString)) {
                    Text(
                        text = if (direct) card.word else card.translationAsString,
                        fontSize = 35.sp,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )
                } else {
                    TextWithPopup(
                        shortText = shortText(card.translationAsString),
                        fullText = card.translationAsString,
                        fontSize = 30.sp,
                        lineHeight = 32.sp,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.weight(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "[${(card.answered * 100) / dictionary.numberOfRightAnswers}%]",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (direct) {
                        AudioPlayerIcon(
                            ttsViewModel = ttsViewModel,
                            card = card,
                            modifier = Modifier.size(64.dp),
                            size = 64.dp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FadeLazyColumn(
                modifier = Modifier
                    .weight(2f)
                    .border(BorderStroke(1.dp, Color.Gray))
                    .padding(16.dp)
            ) {
                currentCard.value?.let { leftCard ->
                    items(
                        cardsMap.value[leftCard] ?: emptyList(),
                        key = { checkNotNull(it.cardId) }) { option ->
                        if (direct) {
                            TableCellTranslation(
                                optionCard = option,
                                onSelectItem = { onOptionSelected(option) },
                                isSelected = selectedOption.value == option,
                                borderColor = color(
                                    isSelected = selectedOption.value == option,
                                    isCorrect = isCorrect.value
                                ),
                            )
                        } else {
                            TableCellWord(
                                optionCard = option,
                                onSelectItem = { onOptionSelected(option) },
                                isSelected = selectedOption.value == option,
                                borderColor = color(
                                    isSelected = selectedOption.value == option,
                                    isCorrect = isCorrect.value
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCellTranslation(
    optionCard: CardEntity,
    onSelectItem: () -> Unit,
    isSelected: Boolean,
    borderColor: Color,
) {
    if (isTextShort(optionCard.translationAsString)) {
        TableCellSelectable(
            text = optionCard.translationAsString,
            isSelected = isSelected,
            borderColor = borderColor,
            onSelect = {
                onSelectItem()
            }
        )
    } else {
        TableCellSelectableWithPopup(
            shortText = shortText(optionCard.translationAsString),
            fullText = optionCard.translationAsString,
            isSelected = isSelected,
            borderColor = borderColor,
            onSelect = {
                onSelectItem()
            }
        )
    }
}

@Composable
private fun TableCellWord(
    optionCard: CardEntity,
    onSelectItem: () -> Unit,
    isSelected: Boolean,
    borderColor: Color,
) {
    TableCellSelectable(
        text = optionCard.word,
        isSelected = isSelected,
        borderColor = borderColor,
        onSelect = {
            onSelectItem()
        }
    )
}
