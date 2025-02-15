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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    onSignOut: () -> Unit = {},
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

    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val leftCards = cardViewModel.unknownDeckCards { id ->
        dictionaryViewModel.dictionaryById(id).numberOfRightAnswers
    }.shuffled().take(settings.numberOfWordsPerStage)
    val rightCards = cardViewModel.cardsDeck.value.shuffled()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
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
                initialLeftCards = leftCards,
                initialRightCards = rightCards,
                direct = direction,
                onNextStage = onNextStage,
                ttsViewModel = ttsViewModel,
            )
        }
    }
}

@Composable
fun MosaicPanels(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    ttsViewModel: TTSViewModel,
    initialLeftCards: List<CardEntity>,
    initialRightCards: List<CardEntity>,
    direct: Boolean,
    onNextStage: () -> Unit,
) {

    val leftCards = remember { mutableStateListOf(*initialLeftCards.toTypedArray()) }
    val rightCards = remember { mutableStateListOf(*initialRightCards.toTypedArray()) }

    val selectedLeftItem = remember { mutableStateOf<CardEntity?>(null) }
    val selectedRightItem = remember { mutableStateOf<CardEntity?>(null) }

    val isAnswerProcessing = remember { mutableStateOf(false) }
    val isCorrectAnswerProcessing = remember { mutableStateOf(false) }

    fun match(): Boolean {
        val leftId = selectedLeftItem.value?.cardId
        val rightId = selectedRightItem.value?.cardId
        return leftId != null && rightId != null && leftId == rightId
    }

    fun notMatch(): Boolean {
        val leftId = selectedLeftItem.value?.cardId
        val rightId = selectedRightItem.value?.cardId
        return leftId != null && rightId != null && leftId != rightId
    }

    fun color(leftItem: CardEntity?, rightItem: CardEntity?): Color {
        return when {
            match() -> Color.Green
            isCorrectAnswerProcessing.value -> Color.Gray
            notMatch() -> Color.Red
            leftItem != null || rightItem != null -> Color.Blue
            else -> Color.Gray
        }
    }

    fun onSelectItem() {
        cardViewModel.viewModelScope.launch {
            if (isAnswerProcessing.value) {
                Log.d(tag, "Click ignored: Answer is being processed!")
                return@launch
            }
            isAnswerProcessing.value = true
            try {
                val leftItem = selectedLeftItem.value
                val rightItem = selectedRightItem.value
                Log.d(
                    tag,
                    "left = ${leftItem?.cardId}('${leftItem?.word}'), right = ${rightItem?.cardId}('${rightItem?.word}')"
                )

                if (leftItem == null || rightItem == null) {
                    Log.d(tag, "One of the items is null. No further action.")
                    return@launch
                }

                val cardId =
                    checkNotNull(if (direct) leftItem.cardId else rightItem.cardId) { "no card id" }

                if (match()) {
                    isCorrectAnswerProcessing.value = true
                    val dictionaryId = checkNotNull(leftItem.dictionaryId) { "no dictionary id" }
                    val dictionary = dictionaryViewModel.dictionaryById(dictionaryId)

                    delay(STAGE_MOSAIC_CELL_DELAY_MS)
                    ttsViewModel.waitForAudionProcessing(cardId)
                    leftCards.remove(leftItem)
                    rightCards.remove(rightItem)
                    selectedLeftItem.value = null
                    selectedRightItem.value = null

                    val wrongAnyway = cardViewModel.wrongAnsweredCardDeckIds.value.contains(cardId)
                    Log.i(
                        tag,
                        "Correct answer for card ${cardId}(${leftItem.word})" +
                                if (wrongAnyway) ", but it is already marked as wrong" else ""
                    )
                    cardViewModel.updateDeckCard(
                        cardId = cardId,
                        numberOfRightAnswers = dictionary.numberOfRightAnswers,
                    )
                    isCorrectAnswerProcessing.value = false
                } else {
                    Log.i(tag, "Wrong answer for card $cardId(${leftItem.word})")
                    delay(STAGE_MOSAIC_CELL_DELAY_MS)
                    ttsViewModel.waitForAudionProcessing(cardId)
                    selectedLeftItem.value = null
                    selectedRightItem.value = null
                    cardViewModel.markDeckCardAsWrong(cardId)
                }

                if (leftCards.isEmpty()) {
                    Log.d(tag, "All left cards are matched. Moving to the next stage.")
                    onNextStage()
                } else {
                    Log.d(tag, "Left cards: ${leftCards.size}, Right cards: ${rightCards.size}")
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
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .border(BorderStroke(1.dp, Color.Gray))
        ) {
            items(leftCards, key = { checkNotNull(it.cardId) }) { item ->
                if (direct) {
                    TableCellSelectable(
                        text = item.word,
                        isSelected = selectedLeftItem.value?.cardId == item.cardId,
                        borderColor = color(
                            leftItem = selectedLeftItem.value,
                            rightItem = selectedRightItem.value,
                        ),
                        onSelect = {
                            ttsViewModel.loadAndPlayAudio(item)
                            selectedLeftItem.value = item
                            onSelectItem()
                        }
                    )
                } else {
                    TableCellTranslation(
                        item = item,
                        selectedItem = selectedLeftItem,
                        onSelectItem = { onSelectItem() },
                        color = {
                            color(
                                leftItem = selectedLeftItem.value,
                                rightItem = selectedRightItem.value,
                            )
                        },
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .border(BorderStroke(1.dp, Color.Gray))
        ) {
            items(rightCards, key = { checkNotNull(it.cardId) }) { item ->
                if (direct) {
                    TableCellTranslation(
                        item = item,
                        selectedItem = selectedRightItem,
                        onSelectItem = { onSelectItem() },
                        color = {
                            color(
                                leftItem = selectedLeftItem.value,
                                rightItem = selectedRightItem.value,
                            )
                        },
                    )
                } else {
                    TableCellSelectable(
                        text = item.word,
                        isSelected = selectedRightItem.value?.cardId == item.cardId,
                        borderColor = color(
                            leftItem = selectedLeftItem.value,
                            rightItem = selectedRightItem.value,
                        ),
                        onSelect = {
                            ttsViewModel.loadAndPlayAudio(item)
                            selectedRightItem.value = item
                            onSelectItem()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCellTranslation(
    item: CardEntity,
    selectedItem: MutableState<CardEntity?>,
    onSelectItem: () -> Unit,
    color: () -> Color,
) {
    if (isTextShort(item.translationAsString)) {
        TableCellSelectable(
            text = item.translationAsString,
            isSelected = selectedItem.value?.cardId == item.cardId,
            borderColor = color(),
            onSelect = {
                selectedItem.value = item
                onSelectItem()
            }
        )
    } else {
        TableCellSelectableWithPopup(
            shortText = shortText(item.translationAsString),
            fullText = item.translationAsString,
            isSelected = selectedItem.value?.cardId == item.cardId,
            borderColor = color(),
            onSelect = {
                selectedItem.value = item
                onSelectItem()
            }
        )
    }
}
