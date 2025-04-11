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
import androidx.compose.runtime.saveable.rememberSaveable
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

            OptionsPanel(
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
fun OptionsPanel(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onNextStage: () -> Unit,
    direct: Boolean,
) {

    val isNavigatingToNextStage = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(tutorViewModel.stageOptionsCardsMap.value) {
        if (
            tutorViewModel.stageOptionsCardsMap.value.isEmpty() &&
            tutorViewModel.stageOptionsCurrentCard.value == null &&
            !isNavigatingToNextStage.value
        ) {
            isNavigatingToNextStage.value = true
            tutorViewModel.clearFlashcardsSessionState()
            onNextStage()
        }
    }

    if (isNavigatingToNextStage.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }

    tutorViewModel.initStageOptions(
        selectNumberOfRightAnswers = { id ->
            dictionariesViewModel.dictionaryById(id).numberOfRightAnswers
        },
        numberOfWordsPerStage = settings.numberOfWordsPerStage,
    )

    val hasPlayedAudio = rememberSaveable { mutableStateOf(false) }
    if (direct && !hasPlayedAudio.value) {
        tutorViewModel.stageOptionsCurrentCard.value?.let { first ->
            LaunchedEffect(first.cardId) {
                ttsViewModel.loadAndPlayAudio(first)
                ttsViewModel.waitForAudionProcessing(checkNotNull(first.cardId))
                hasPlayedAudio.value = true
            }
        }
    }

    if (tutorViewModel.stageOptionsLeftCards.value.isEmpty()) {
        Log.d(tag, "onNextStage: stageOptionsLeftCards is empty")
        onNextStage()
        return
    }

    LaunchedEffect(Unit) {
        if (!tutorViewModel.isAdditionalDeckLoaded) {
            val rightCardsSize =
                tutorViewModel.stageOptionsLeftCards.value.size * (settings.stageOptionsNumberOfVariants - 1)
            tutorViewModel.loadAdditionalCardDeck(
                dictionaryIds = dictionariesViewModel.selectedDictionaryIds.value,
                length = rightCardsSize
            )
            tutorViewModel.markAdditionalDeckLoaded()
        }
    }
    if (tutorViewModel.isAdditionalCardsDeckLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
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

    tutorViewModel.generateOptionsCardsMap(settings.stageOptionsNumberOfVariants)

    val currentCard = tutorViewModel.stageOptionsCurrentCard
    val selectedOption = tutorViewModel.stageOptionsSelectedOption
    val isCorrect = tutorViewModel.stageOptionsIsCorrect
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
        isNavigatingToNextStage.value = true
        selectedOption.value = null
        isCorrect.value = null
        tutorViewModel.clearFlashcardsSessionState()
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
                    val nextMap = tutorViewModel.stageOptionsCardsMap.value.toMutableMap()
                    currentCard.value?.let {
                        nextMap.remove(it)
                    }
                    val nextCard = nextMap.keys.firstOrNull()

                    if (nextCard == null) {
                        Log.d(tag, "onNextStage: null next card")
                        resetState()
                        onNextStage()
                        return@launch
                    }

                    tutorViewModel.stageOptionsCardsMap.value = nextMap
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

    Log.d(
        tag,
        "current word: [${currentCard.value?.cardId}: ${currentCard.value?.word}]; " +
                "options ${tutorViewModel.stageOptionsCardsMap.value[currentCard.value]?.size}"
    )

    val card = currentCard.value
    if (card == null) {
        Log.d(tag, "onNextStage: null current card")
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
                        items = tutorViewModel.stageOptionsCardsMap.value[leftCard] ?: emptyList(),
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
