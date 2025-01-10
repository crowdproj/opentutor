package com.github.sszuev.flashcards.android.ui

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

@Composable
fun StageMosaicScreen(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNext: () -> Unit = {},
    direction: Boolean = true,
) {
    BackHandler {
        onHomeClick()
    }

    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val leftCards = cardViewModel.unknownDeckCards { id ->
        dictionaryViewModel.selectedDictionariesList.first { id == it.dictionaryId }.numberOfRightAnswers
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

            MosaicPanel(cardViewModel, leftCards, rightCards)

            Button(
                onClick = onNext,
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
    leftCards: List<CardEntity>,
    rightCards: List<CardEntity>
) {
    val selectedLeftItem = remember { mutableStateOf<CardEntity?>(null) }
    val selectedRightItem = remember { mutableStateOf<CardEntity?>(null) }

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
                    onSelect = {
                        cardViewModel.loadAndPlayAudio(item)
                        selectedLeftItem.value = item
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
                        onSelect = { selectedRightItem.value = item }
                    )
                } else {
                    TableCellSelectableWithPopup(
                        shortText = shortText(item.translation),
                        fullText = item.translation,
                        isSelected = selectedRightItem.value == item,
                        onSelect = { selectedRightItem.value = item }
                    )
                }
            }
        }
    }
}