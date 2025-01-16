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
import androidx.compose.foundation.lazy.LazyColumn
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
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "StageOptionsUI"

@Composable
fun StageOptionsScreen(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageOptions")
    if (cardViewModel.cardsDeck.value.isEmpty()) {
        return
    }
    if (dictionaryViewModel.selectedDictionariesList.isEmpty()) {
        return
    }
    BackHandler {
        onHomeClick()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
            Text(
                text = "Stage: options [${if (direction) "source -> target" else "target -> source"}]",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OptionsPanelDirect(
                cardViewModel = cardViewModel,
                dictionaryViewModel = dictionaryViewModel,
                settingsViewModel = settingsViewModel,
                onNextStage = onNextStage,
                direct = direction,
            )
        }
    }
}

@Composable
fun OptionsPanelDirect(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    onNextStage: () -> Unit,
    direct: Boolean,
) {
    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val leftCards = cardViewModel.unknownDeckCards { id ->
        checkNotNull(dictionaryViewModel.selectedDictionariesList
            .singleOrNull { id == it.dictionaryId }) { "Can't find dictionary by id $id" }
            .numberOfRightAnswers
    }.shuffled().take(settings.numberOfWordsPerStage)

    if (leftCards.isEmpty()) {
        onNextStage()
        return
    }

    val rightCardsSize = leftCards.size * (settings.stageOptionsNumberOfVariants - 1)
    LaunchedEffect(Unit) {
        cardViewModel.loadAdditionalCardDeck(
            dictionaryIds = dictionaryViewModel.selectedDictionaryIds.value,
            length = rightCardsSize
        )
    }
    if (cardViewModel.isAdditionalCardsDeckLoading.value) {
        CircularProgressIndicator()
        return
    }

    val errorMessage = cardViewModel.errorMessage.value
    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (cardViewModel.additionalCardsDeck.value.size <= 1) {
        onNextStage()
        return
    }

    val cardsMap = remember {
        leftCards.associateWith { leftCard ->
            val rightCards = cardViewModel.additionalCardsDeck.value.shuffled()
                .take(settings.stageOptionsNumberOfVariants - 1)
            (rightCards + leftCard).distinct().shuffled()
        }.toMutableMap()
    }

    val currentCard = remember { mutableStateOf(leftCards.firstOrNull()) }
    val selectedOption = remember { mutableStateOf<CardEntity?>(null) }
    val isCorrect = remember { mutableStateOf<Boolean?>(null) }

    if (direct) {
        LaunchedEffect(currentCard.value) {
            if (currentCard.value == null) {
                onNextStage()
                return@LaunchedEffect
            }
            currentCard.value?.let { card ->
                cardViewModel.loadAndPlayAudio(card)
            }
        }
    }

    fun match(selectedItem: CardEntity): Boolean {
        return selectedItem.cardId == currentCard.value?.cardId
    }

    fun onOptionSelected(selectedItem: CardEntity) {
        selectedOption.value = selectedItem
        isCorrect.value = match(selectedItem)

        if (!direct) {
            cardViewModel.loadAndPlayAudio(selectedItem)
        }

        cardViewModel.viewModelScope.launch {
            delay(STAGE_OPTIONS_CELL_DELAY_MS)
            if (isCorrect.value == true) {
                val card = cardsMap.keys.first()
                val cardId = checkNotNull(card.cardId)

                val dictionaryId = checkNotNull(card.dictionaryId) { "no dictionary id" }
                val dictionary = dictionaryViewModel.dictionaryById(dictionaryId)

                currentCard.value = card

                val wrongAnyway = cardViewModel.wrongAnsweredCardDeckIds.value.contains(cardId)
                Log.i(
                    tag,
                    "Correct answer for card ${cardId}${if (wrongAnyway) ", but it is already marked as wrong" else ""}"
                )

                cardViewModel.updateDeckCard(
                    cardId,
                    dictionary.numberOfRightAnswers
                )

                cardsMap.remove(currentCard.value)
                currentCard.value = cardsMap.keys.firstOrNull()
                selectedOption.value = null
                isCorrect.value = null

                if (cardsMap.isEmpty()) {
                    onNextStage()
                }
            } else {
                val cardId = checkNotNull(currentCard.value?.cardId)
                Log.i(tag, "Wrong answer for card $cardId")
                selectedOption.value = null
                isCorrect.value = null
                cardViewModel.markDeckCardAsWrong(cardId)
            }
        }
    }

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
                val card = currentCard.value
                if (card == null) {
                    onNextStage()
                    return
                }
                val dictionary =
                    dictionaryViewModel.dictionaryById(checkNotNull(card.dictionaryId))
                if (direct || isTextShort(card.translation)) {
                    Text(
                        text = if (direct) card.word else card.translation,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )
                } else {
                    TextWithPopup(
                        shortText = shortText(card.translation),
                        fullText = card.translation,
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
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
                            viewModel = cardViewModel,
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
            LazyColumn(
                modifier = Modifier
                    .weight(2f)
                    .border(BorderStroke(1.dp, Color.Gray))
                    .padding(16.dp)
            ) {
                currentCard.value?.let { leftCard ->
                    items(
                        cardsMap[leftCard] ?: emptyList(),
                        key = { checkNotNull(it.cardId) }) { option ->
                        if (direct) {
                            TableCellTranslation(
                                optionCard = option,
                                isSelected = selectedOption.value == option,
                                isCorrect = isCorrect.value,
                                onSelectItem = { onOptionSelected(option) }
                            )
                        } else {
                            TableCellWord(
                                optionCard = option,
                                isSelected = selectedOption.value == option,
                                isCorrect = isCorrect.value,
                                onSelectItem = { onOptionSelected(option) }
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
    isCorrect: Boolean?,
) {
    val borderColor = when {
        isSelected && isCorrect == true -> Color.Green
        isSelected && isCorrect == false -> Color.Red
        else -> Color.Gray
    }
    if (isTextShort(optionCard.translation)) {
        TableCellSelectable(
            text = optionCard.translation,
            isSelected = isSelected,
            borderColor = borderColor,
            onSelect = {
                onSelectItem()
            }
        )
    } else {
        TableCellSelectableWithPopup(
            shortText = shortText(optionCard.translation),
            fullText = optionCard.translation,
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
    isCorrect: Boolean?,
) {
    val borderColor = when {
        isSelected && isCorrect == true -> Color.Green
        isSelected && isCorrect == false -> Color.Red
        else -> Color.Gray
    }
    TableCellSelectable(
        text = optionCard.word,
        isSelected = isSelected,
        borderColor = borderColor,
        onSelect = {
            onSelectItem()
        }
    )
}
