package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.STAGE_MOSAIC_CELL_DELAY_MS
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

private const val tag = "StageMosaicUI"

@Composable
fun StageMosaicScreen(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageMosaic")
    if (tutorViewModel.cardsDeck.value.isEmpty()) {
        onNextStage()
        return
    }
    BackHandler {
        onHomeClick()
    }
    if (dictionariesViewModel.selectedDictionaryIds.value.isEmpty()) {
        return
    }

    val settings = checkNotNull(settingsViewModel.settings.value)

    val isNavigatingToNextStage = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tutorViewModel.initStageMosaic(
            selectNumberOfRightAnswers = { dictionariesViewModel.dictionaryById(it).numberOfRightAnswers },
            numberOfWords = settings.numberOfWordsPerStage
        )
    }

    LaunchedEffect(tutorViewModel.stageMosaicLeftCards.value) {
        if (!isNavigatingToNextStage.value && tutorViewModel.stageMosaicLeftCards.value.isEmpty()) {
            Log.d(tag, "StageMosaic is empty, going to next stage")
            isNavigatingToNextStage.value = true
            tutorViewModel.clearFlashcardsSessionState()
            onNextStage()
        }
    }

    if (isNavigatingToNextStage.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Stage: mosaic [${if (direction) "source -> target" else "target -> source"}]",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            MosaicPanels(
                tutorViewModel = tutorViewModel,
                dictionariesViewModel = dictionariesViewModel,
                cardsViewModel = cardsViewModel,
                ttsViewModel = ttsViewModel,
                direct = direction,
                onNextStage = onNextStage,
            )
        }
    }
}

@Composable
fun MosaicPanels(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    ttsViewModel: TTSViewModel,
    direct: Boolean,
    onNextStage: () -> Unit,
) {
    val leftCards = tutorViewModel.stageMosaicLeftCards
    val rightCards = tutorViewModel.stageMosaicRightCards
    val selectedLeftId = tutorViewModel.stageMosaicSelectedLeftCardId
    val selectedRightId = tutorViewModel.stageMosaicSelectedRightCardId

    val deck = tutorViewModel.cardsDeck.value
    val isAnswerProcessing = rememberSaveable { mutableStateOf(false) }
    val isCorrectAnswerProcessing = rememberSaveable { mutableStateOf(false) }

    val errorMessage = tutorViewModel.errorMessage.value
    if (errorMessage != null) {
        Log.e(tag, errorMessage)
        return
    }

    fun match(): Boolean =
        selectedLeftId.value != null && selectedRightId.value != null &&
                selectedLeftId.value == selectedRightId.value

    fun notMatch(): Boolean =
        selectedLeftId.value != null && selectedRightId.value != null &&
                selectedLeftId.value != selectedRightId.value

    fun color(leftId: String?, rightId: String?): Color = when {
        match() -> Color.Green
        isCorrectAnswerProcessing.value -> Color.Gray
        notMatch() -> Color.Red
        leftId != null || rightId != null -> Color.Blue
        else -> Color.Gray
    }

    fun onSelectItem() {
        tutorViewModel.viewModelScope.launch {
            if (isAnswerProcessing.value) {
                return@launch
            }
            isAnswerProcessing.value = true
            try {
                val leftItem = deck.firstOrNull { it.cardId == selectedLeftId.value }
                val rightItem = deck.firstOrNull { it.cardId == selectedRightId.value }
                Log.d(
                    tag,
                    "left = ${leftItem?.cardId}('${leftItem?.word}'), right = ${rightItem?.cardId}('${rightItem?.word}')"
                )
                if (leftItem == null || rightItem == null) {
                    return@launch
                }

                val card = if (direct) leftItem else rightItem
                val cardId = card.cardId
                val dictionary = dictionariesViewModel.dictionaryById(leftItem.dictionaryId!!)

                if (match()) {
                    isCorrectAnswerProcessing.value = true
                    delay(STAGE_MOSAIC_CELL_DELAY_MS)
                    ttsViewModel.waitForAudionProcessing(checkNotNull(cardId))
                    tutorViewModel.stageMosaicLeftCards.value =
                        leftCards.value.filter { it.cardId != cardId }
                    tutorViewModel.stageMosaicRightCards.value =
                        rightCards.value.filter { it.cardId != cardId }

                    val wrongAnyway = tutorViewModel.wrongAnsweredCardDeckIds.value.contains(cardId)
                    Log.i(
                        tag,
                        "Correct answer for card ${cardId}(${card.word})" +
                                if (wrongAnyway) ", but it is already marked as wrong" else ""
                    )
                    tutorViewModel.updateDeckCard(
                        cardId = cardId,
                        numberOfRightAnswers = dictionary.numberOfRightAnswers,
                        updateCard = { cardsViewModel.updateCard(it) }
                    )
                    isCorrectAnswerProcessing.value = false
                } else {
                    Log.i(tag, "Wrong answer for card $cardId(${card.word})")
                    delay(STAGE_MOSAIC_CELL_DELAY_MS)
                    ttsViewModel.waitForAudionProcessing(cardId!!)
                    tutorViewModel.markDeckCardAsWrong(cardId)
                }

                selectedLeftId.value = null
                selectedRightId.value = null

                if (tutorViewModel.stageMosaicLeftCards.value.isEmpty()) {
                    Log.i(tag, "All left cards are matched. Moving to the next stage.")
                    tutorViewModel.clearFlashcardsSessionState()
                    onNextStage()
                } else {
                    Log.d(
                        tag,
                        "Left cards: ${tutorViewModel.stageMosaicLeftCards.value.size}, " +
                                "Right cards: ${tutorViewModel.stageMosaicRightCards.value.size}"
                    )
                }
            } finally {
                isAnswerProcessing.value = false
            }
        }
    }

    Log.d(tag, "Left-cards (${if (direct) "direct" else "reverse"}): ${leftCards.value.size}")
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FadeLazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .border(BorderStroke(1.dp, Color.Gray))
        ) {
            items(items = leftCards.value, key = { checkNotNull(it.cardId) }) { item ->
                val isSelected = selectedLeftId.value == item.cardId
                val border = color(item.cardId, selectedRightId.value)
                val onClick = {
                    if (direct) {
                        ttsViewModel.loadAndPlayAudio(item)
                    }
                    selectedLeftId.value = item.cardId
                    onSelectItem()
                }
                if (direct) {
                    TableCellSelectable(
                        text = item.word,
                        isSelected = isSelected,
                        onSelect = onClick,
                        borderColor = border
                    )
                } else {
                    TableCellTranslation(
                        optionCard = item,
                        isSelected = isSelected,
                        borderColor = border,
                        onSelectItem = onClick,
                    )
                }
            }
        }

        FadeLazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .border(BorderStroke(1.dp, Color.Gray))
        ) {
            items(items = rightCards.value, key = { checkNotNull(it.cardId) }) { item ->
                val isSelected = selectedRightId.value == item.cardId
                val border = color(selectedLeftId.value, item.cardId)
                val onClick = {
                    if (!direct) {
                        ttsViewModel.loadAndPlayAudio(item)
                    }
                    selectedRightId.value = item.cardId
                    onSelectItem()
                }
                if (direct) {
                    TableCellTranslation(
                        optionCard = item,
                        isSelected = isSelected,
                        borderColor = border,
                        onSelectItem = onClick,
                    )
                } else {
                    TableCellSelectable(
                        text = item.word,
                        isSelected = isSelected,
                        onSelect = onClick,
                        borderColor = border
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCellTranslation(
    optionCard: CardEntity,
    isSelected: Boolean,
    borderColor: Color,
    onSelectItem: () -> Unit,
) {
    if (isTextShort(optionCard.translationAsString)) {
        TableCellSelectable(
            text = optionCard.translationAsString,
            isSelected = isSelected,
            borderColor = borderColor,
            onSelect = onSelectItem
        )
    } else {
        TableCellSelectableWithPopup(
            shortText = shortText(optionCard.translationAsString),
            fullText = optionCard.translationAsString,
            isSelected = isSelected,
            borderColor = borderColor,
            onSelect = onSelectItem
        )
    }
}