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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText

private const val tag = "StageMosaicUI"

@Composable
fun StageMosaicScreen(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
    onResultStage: () -> Unit = {},
) {
    Log.d(tag, "StageMosaic")
    if (cardViewModel.cardsDeck.value.isEmpty()) {
        return
    }
    if (dictionaryViewModel.selectedDictionariesList.isEmpty()) {
        return
    }
    BackHandler {
        onHomeClick()
    }

    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val leftCards = cardViewModel.unknownDeckCards { id ->
        checkNotNull(dictionaryViewModel.selectedDictionariesList
            .singleOrNull { id == it.dictionaryId }) { "Can't find dictionary by id $id" }
            .numberOfRightAnswers
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

            MosaicPanel(
                cardViewModel = cardViewModel,
                dictionaryViewModel = dictionaryViewModel,
                initialLeftCards = leftCards,
                initialRightCards = rightCards,
                onNextStage = onNextStage,
                onResultStage = onResultStage,
            )

            Button(
                onClick = onNextStage,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("NEXT")
            }
        }
    }
}

@Composable
fun MosaicPanel(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    initialLeftCards: List<CardEntity>,
    initialRightCards: List<CardEntity>,
    onNextStage: () -> Unit,
    onResultStage: () -> Unit,
) {

    val selectedDictionariesMap =
        dictionaryViewModel.selectedDictionariesList.associateBy { it.dictionaryId }

    val leftCards = remember { mutableStateListOf(*initialLeftCards.toTypedArray()) }
    val rightCards = remember { mutableStateListOf(*initialRightCards.toTypedArray()) }

    val selectedLeftItem = remember { mutableStateOf<CardEntity?>(null) }
    val selectedRightItem = remember { mutableStateOf<CardEntity?>(null) }

    fun match(): Boolean {
        return selectedRightItem.value?.cardId == selectedLeftItem.value?.cardId
    }

    fun color(): Color {
        return when {
            selectedLeftItem.value == null || selectedRightItem.value == null -> Color.Blue
            match() -> Color.Green
            else -> Color.Red
        }
    }

    fun dictionaryNumberOfRightAnswers(card: CardEntity): Int {
        return checkNotNull(
            selectedDictionariesMap[checkNotNull(card.dictionaryId) { "can't find dictionary, card = $card" }]
        ) { "can't find dictionary, dictionaryId = ${card.dictionaryId}" }.numberOfRightAnswers
    }

    fun onSelectItem() {
        val leftItem = selectedLeftItem.value
        val rightItem = selectedRightItem.value

        if (leftItem != null && rightItem != null) {
            val cardId = checkNotNull(leftItem.cardId) { "no card id" }
            if (match()) {
                val dictionaryId = checkNotNull(leftItem.dictionaryId) { "no dictionary id" }
                val dictionary = checkNotNull(selectedDictionariesMap[dictionaryId]) {
                    "unable to find dictionary $dictionaryId"
                }
                leftCards.remove(leftItem)
                rightCards.remove(rightItem)
                selectedLeftItem.value = null
                selectedRightItem.value = null
                val wrongAnyway = cardViewModel.wrongAnsweredCardDeckIds.value.contains(cardId)
                Log.i(
                    tag,
                    "Correct answer for card ${cardId}${if (wrongAnyway) ", but it is already marked as wrong" else ""}"
                )
                cardViewModel.updateDeckCard(
                    cardId = cardId,
                    numberOfRightAnswers = dictionary.numberOfRightAnswers,
                )
                if (cardViewModel.allDeckCardsAnsweredCorrectly {
                        dictionaryNumberOfRightAnswers(it)
                    }) {
                    onResultStage()
                }
            } else {
                Log.i(tag, "Wrong answer for card $cardId")
                cardViewModel.markDeckCardAsWrong(cardId)
            }
        }
        if (leftCards.isEmpty()) {
            onNextStage()
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
            items(leftCards) { item ->
                TableCellSelectable(
                    text = item.word,
                    isSelected = selectedLeftItem.value == item,
                    borderColor = color(),
                    onSelect = {
                        cardViewModel.loadAndPlayAudio(item)
                        selectedLeftItem.value = item
                        onSelectItem()
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .border(BorderStroke(1.dp, Color.Gray))
        ) {
            items(rightCards) { item ->
                if (isTextShort(item.translation)) {
                    TableCellSelectable(
                        text = item.translation,
                        isSelected = selectedRightItem.value == item,
                        borderColor = color(),
                        onSelect = {
                            selectedRightItem.value = item
                            onSelectItem()
                        }
                    )
                } else {
                    TableCellSelectableWithPopup(
                        shortText = shortText(item.translation),
                        fullText = item.translation,
                        isSelected = selectedRightItem.value == item,
                        borderColor = color(),
                        onSelect = {
                            selectedRightItem.value = item
                            onSelectItem()
                        }
                    )
                }
            }
        }
    }
}
