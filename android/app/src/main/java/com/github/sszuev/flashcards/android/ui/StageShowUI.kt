package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.STAGE_SHOW_BUTTONS_DELAY_MS
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import kotlinx.coroutines.delay

private const val tag = "StageShowUI"

@Composable
fun StageShowScreen(
    dictionaryViewModel: DictionaryViewModel,
    cardViewModel: CardViewModel,
    settingsViewModel: SettingsViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    onResultStage: () -> Unit = {},
) {

    if (dictionaryViewModel.selectedDictionaryIds.value.isEmpty()) {
        return
    }

    BackHandler {
        onHomeClick()
    }

    val cards = cardViewModel.cardsDeck.value
    val currentCardIndex = cardViewModel.stageShowCurrentDeckCardIndex.value
    val isLoading = cardViewModel.isCardsDeckLoading.value
    val errorMessage = cardViewModel.errorMessage.value
    val settings = checkNotNull(settingsViewModel.settings.value)

    LaunchedEffect(dictionaryViewModel.selectedDictionaryIds.value) {
        cardViewModel.loadNextCardDeck(
            dictionaryViewModel.selectedDictionaryIds.value,
            length = settings.stageShowNumberOfWords,
        )
    }

    LaunchedEffect(cardViewModel.cardsDeck.value) {
        val currentCards = cardViewModel.cardsDeck.value
        Log.d(tag, "Cards updated: ${currentCards.size}")
        if (currentCards.isNotEmpty()) {
            Log.d(tag, "Playing audio for: ${currentCards.first().word}")
            cardViewModel.loadAndPlayAudio(currentCards.first())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)

            Text(
                text = "Stage: show",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return
            }

            if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
                return
            }

            if (cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No cards available.", color = MaterialTheme.colorScheme.error)
                }
                return
            }

            val currentCard = cards[currentCardIndex]
            val currentDictionary =
                dictionaryViewModel.dictionaryById(checkNotNull(cards[currentCardIndex].dictionaryId))

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
                    Text(
                        text = currentCard.word,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )
                    Row(
                        modifier = Modifier.weight(0.5f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "[${(currentCard.answered * 100) / currentDictionary.numberOfRightAnswers}%]",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        AudioPlayerIcon(
                            viewModel = cardViewModel,
                            card = currentCard,
                            modifier = Modifier.size(64.dp),
                            size = 64.dp
                        )
                    }
                }

                Text(
                    text = currentCard.translation,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                )
            }

            currentCard.examples.forEach { example ->
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .align(Alignment.Start)
                )
            }
        }
        val currentCard = cards[currentCardIndex]
        val currentDictionary =
            dictionaryViewModel.dictionaryById(checkNotNull(currentCard.dictionaryId))

        var buttonsEnabled by remember { mutableStateOf(false) }

        val alpha by animateFloatAsState(if (buttonsEnabled) 1f else 0.5f)

        LaunchedEffect(currentCard) {
            buttonsEnabled = false
            delay(STAGE_SHOW_BUTTONS_DELAY_MS)
            buttonsEnabled = true
        }

        StageShowBottomToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(alpha),
            buttonsEnabled = buttonsEnabled,
            onNextCardDeck = {
                if (!cardViewModel.nextDeckCard(
                        numberOfRightAnswers = currentDictionary.numberOfRightAnswers,
                        onNextCard = { cardViewModel.loadAndPlayAudio(it) })
                ) {
                    onNextStage()
                }
            },
            onKnown = {
                cardViewModel.updateShowStageCurrentDeckCardAsAnsweredCorrectly(
                    numberOfRightAnswers = currentDictionary.numberOfRightAnswers,
                    onResultStage = onResultStage
                )
            },
        )
    }
}

@Composable
fun StageShowBottomToolbar(
    modifier: Modifier = Modifier,
    buttonsEnabled: Boolean,
    onNextCardDeck: () -> Unit = {},
    onKnown: () -> Unit = {},
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
                label = "KNOW",
                containerWidthDp = containerWidthDp,
                weight = 50f,
                onClick = onKnown,
                enabled = buttonsEnabled,
            )
            ToolbarButton(
                label = "NEXT",
                containerWidthDp = containerWidthDp,
                weight = 50.0f,
                onClick = onNextCardDeck,
                enabled = buttonsEnabled,
            )
        }
    }
}