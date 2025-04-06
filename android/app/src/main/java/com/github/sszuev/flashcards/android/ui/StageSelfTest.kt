package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString


private const val tag = "StageSelfTestUI"

@Composable
fun StageSelfTestScreen(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageSelfTest")
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
                text = "Stage: self-test [${if (direction) "source -> target" else "target -> source"}]",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            SelfTestPanels(
                tutorViewModel = tutorViewModel,
                dictionariesViewModel = dictionariesViewModel,
                cardsViewModel = cardsViewModel,
                settingsViewModel = settingsViewModel,
                onNextStage = onNextStage,
                direct = direction,
                ttsViewModel = ttsViewModel,
            )
        }
    }
}

@Composable
fun SelfTestPanels(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onNextStage: () -> Unit = {},
    direct: Boolean = true,
) {
    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val cards = remember {
        tutorViewModel.unknownDeckCards { id ->
            dictionariesViewModel.dictionaryById(id).numberOfRightAnswers
        }.shuffled().take(settings.numberOfWordsPerStage).toMutableList()
    }

    if (cards.isEmpty()) {
        onNextStage()
        return
    }

    val errorMessage = tutorViewModel.errorMessage.value
    ErrorMessageBox(errorMessage)
    if (errorMessage != null) {
        return
    }

    val currentCard = remember { mutableStateOf(cards.firstOrNull()) }

    if (direct) {
        LaunchedEffect(currentCard.value) {
            if (currentCard.value == null) {
                onNextStage()
                return@LaunchedEffect
            }
            currentCard.value?.let { card ->
                Log.d(tag, "Playing audio for: ${card.word}")
                ttsViewModel.loadAndPlayAudio(card)
            }
        }
    }

    var isBigButtonVisible by remember { mutableStateOf(true) }
    var buttonsEnabled by remember { mutableStateOf(false) }

    fun onNextCard(result: Boolean) {
        val card = cards[0]
        currentCard.value = card

        cards.removeAt(0)
        currentCard.value = cards.firstOrNull()

        isBigButtonVisible = true
        buttonsEnabled = false

        if (result) {
            val cardId = checkNotNull(card.cardId)
            val dictionary = dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId))
            tutorViewModel.updateDeckCard(
                cardId = cardId,
                numberOfRightAnswers = dictionary.numberOfRightAnswers,
                updateCard = { cardsViewModel.updateCard(it) },
            )
        } else {
            tutorViewModel.markDeckCardAsWrong(checkNotNull(card.cardId))
        }

        if (cards.isEmpty()) {
            onNextStage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val card = currentCard.value
        if (card == null) {
            onNextStage()
            return
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dictionary =
                dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId))

            if (direct || isTextShort(card.translationAsString)) {
                Text(
                    text = if (direct) card.word else card.translationAsString,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f)
                )
            } else {
                val txt = card.translationAsString
                TextWithPopup(
                    shortText = shortText(txt),
                    fullText = txt,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f),
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

        var containerWidthPx by remember { mutableIntStateOf(0) }
        var containerHeightPx by remember { mutableIntStateOf(0) }
        val density = LocalDensity.current
        val containerWidthDp = with(density) { containerWidthPx.toDp() }
        val containerHeightDp = with(density) { containerHeightPx.toDp() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.LightGray)
                .onSizeChanged { size ->
                    containerWidthPx = size.width
                    containerHeightPx = size.height
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isBigButtonVisible) {
                Box(
                    modifier = Modifier
                        .width(containerWidthDp)
                        .height(containerHeightDp)
                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                        .clickable {
                            isBigButtonVisible = false
                            buttonsEnabled = true
                            if (!direct) {
                                Log.d(tag, "Playing audio for: ${card.word}")
                                ttsViewModel.loadAndPlayAudio(card)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (direct) "Display translation" else "Display word",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp,
                            lineHeight = 28.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Text(
                    text = if (direct) card.translationAsString else card.word,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        StageSelfTestBottomToolbar(
            onCorrect = { onNextCard(true) },
            onWrong = { onNextCard(false) },
            buttonsEnabled = buttonsEnabled,
        )
    }
}

@Composable
private fun StageSelfTestBottomToolbar(
    modifier: Modifier = Modifier,
    buttonsEnabled: Boolean,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
) {
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .padding(8.dp)
            .onSizeChanged { size -> containerWidthPx = size.width },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                label = "CORRECT",
                containerWidthDp = containerWidthDp,
                weight = 50f,
                onClick = onCorrect,
                enabled = buttonsEnabled,
            )
            ToolbarButton(
                label = "WRONG",
                containerWidthDp = containerWidthDp,
                weight = 50f,
                onClick = onWrong,
                enabled = buttonsEnabled,
            )
        }
    }
}