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
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "StageMosaicUI"

@Composable
fun StageMosaicScreen(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageMosaic")
    if (cardViewModel.cardsDeck.value.isEmpty()) {
        onNextStage()
        return
    }
    BackHandler {
        onHomeClick()
    }
    if (dictionaryViewModel.selectedDictionaryIds.value.isEmpty()) {
        return
    }

    val settings = checkNotNull(settingsViewModel.settings.value)
    LaunchedEffect(Unit) {
        cardViewModel.initStageMosaic(
            selectNumberOfRightAnswers = { dictionaryViewModel.dictionaryById(it).numberOfRightAnswers },
            numberOfWords = settings.numberOfWordsPerStage
        )
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
                cardViewModel = cardViewModel,
                dictionaryViewModel = dictionaryViewModel,
                ttsViewModel = ttsViewModel,
                direct = direction,
                onNextStage = onNextStage,
            )
        }
    }
}

@Composable
fun MosaicPanels(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    ttsViewModel: TTSViewModel,
    direct: Boolean,
    onNextStage: () -> Unit,
) {
    val leftCards = cardViewModel.stageMosaicLeftCards
    val rightCards = cardViewModel.stageMosaicRightCards
    val selectedLeftId = cardViewModel.stageMosaicSelectedLeftCardId
    val selectedRightId = cardViewModel.stageMosaicSelectedRightCardId

    val deck = cardViewModel.cardsDeck.value
    val isAnswerProcessing = rememberSaveable { mutableStateOf(false) }
    val isCorrectAnswerProcessing = rememberSaveable { mutableStateOf(false) }

    val errorMessage = cardViewModel.errorMessage.value
    ErrorMessageBox(errorMessage)
    if (errorMessage != null) {
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
        cardViewModel.viewModelScope.launch {
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
                val dictionary = dictionaryViewModel.dictionaryById(leftItem.dictionaryId!!)

                if (match()) {
                    isCorrectAnswerProcessing.value = true
                    delay(STAGE_MOSAIC_CELL_DELAY_MS)
                    ttsViewModel.waitForAudionProcessing(checkNotNull(cardId))
                    cardViewModel.stageMosaicLeftCards.value =
                        leftCards.value.filter { it.cardId != cardId }
                    cardViewModel.stageMosaicRightCards.value =
                        rightCards.value.filter { it.cardId != cardId }

                    val wrongAnyway = cardViewModel.wrongAnsweredCardDeckIds.value.contains(cardId)
                    Log.i(
                        tag,
                        "Correct answer for card ${cardId}(${card.word})" +
                                if (wrongAnyway) ", but it is already marked as wrong" else ""
                    )
                    cardViewModel.updateDeckCard(
                        cardId = cardId,
                        numberOfRightAnswers = dictionary.numberOfRightAnswers
                    )
                    isCorrectAnswerProcessing.value = false
                } else {
                    Log.i(tag, "Wrong answer for card $cardId(${card.word})")
                    delay(STAGE_MOSAIC_CELL_DELAY_MS)
                    ttsViewModel.waitForAudionProcessing(cardId!!)
                    cardViewModel.markDeckCardAsWrong(cardId)
                }

                selectedLeftId.value = null
                selectedRightId.value = null

                if (cardViewModel.stageMosaicLeftCards.value.isEmpty()) {
                    Log.i(tag, "All left cards are matched. Moving to the next stage.")
                    cardViewModel.clearFlashcardsSessionState()
                    onNextStage()
                } else {
                    Log.d(
                        tag,
                        "Left cards: ${cardViewModel.stageMosaicLeftCards.value.size}, " +
                                "Right cards: ${cardViewModel.stageMosaicRightCards.value.size}"
                    )
                }
            } finally {
                isAnswerProcessing.value = false
            }
        }
    }

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
            items(leftCards.value, key = { checkNotNull(it.cardId) }) { item ->
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
            items(rightCards.value, key = { checkNotNull(it.cardId) }) { item ->
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