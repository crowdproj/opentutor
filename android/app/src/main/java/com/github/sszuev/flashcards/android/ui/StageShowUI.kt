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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.translationAsString

private const val tag = "StageShowUI"

@Composable
fun StageShowScreen(
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    tutorViewModel: TutorViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
) {

    if (dictionariesViewModel.selectedDictionaryIds.value.isEmpty()) {
        return
    }

    BackHandler {
        onHomeClick()
    }

    val cards = tutorViewModel.cardsDeck.value
    val isLoading = tutorViewModel.isCardsDeckLoading.value
    val errorMessage = tutorViewModel.errorMessage.value
    val settings = checkNotNull(settingsViewModel.settings.value)

    val deckLoaded = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(dictionariesViewModel.selectedDictionaryIds.value) {
        if (!deckLoaded.value) {
            tutorViewModel.loadNextCardDeck(
                dictionaryIds = dictionariesViewModel.selectedDictionaryIds.value,
                length = settings.stageShowNumberOfWords,
                onComplete = {
                    it.firstOrNull()?.let { card ->
                        ttsViewModel.loadAndPlayAudio(card)
                    }
                }
            )
            deckLoaded.value = true
        }

        // for stage options:
        val shouldLoadAdditionalDeck = settings.stageOptionsSourceLangToTargetLang ||
                settings.stageOptionsTargetLangToSourceLang

        if (shouldLoadAdditionalDeck && !tutorViewModel.isAdditionalDeckLoaded) {
            val rightCardsSize =
                settings.numberOfWordsPerStage * (settings.stageOptionsNumberOfVariants - 1)
            tutorViewModel.loadAdditionalCardDeck(
                dictionaryIds = dictionariesViewModel.selectedDictionaryIds.value,
                length = rightCardsSize
            )
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (cards.isEmpty()) {
        onNextStage()
        return
    }

    val currentCardId = rememberSaveable { mutableStateOf(cards.firstOrNull()?.cardId) }
    val currentCard = cards.firstOrNull { it.cardId == currentCardId.value }

    fun onNextCard(know: Boolean) {
        currentCard?.let { card ->
            if (know) {
                val cardId = checkNotNull(card.cardId)
                val dictionary =
                    dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId))
                tutorViewModel.markDeckCardAsKnow(
                    cardId = cardId,
                    numberOfRightAnswers = dictionary.numberOfRightAnswers,
                    updateCard = { cardsViewModel.updateCard(it) }
                )
            }
        }

        val currentIndex = cards.indexOfFirst { it.cardId == currentCardId.value }
        val nextCard = cards.getOrNull(currentIndex + 1)

        if (nextCard != null) {
            currentCardId.value = nextCard.cardId
            ttsViewModel.loadAndPlayAudio(nextCard)
        } else {
            currentCardId.value = null
            onNextStage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState())
        ) {

            StageHeader("SHOW")

            if (errorMessage != null) {
                Log.e(tag, errorMessage)
                return
            }

            if (currentCard == null) {
                return
            }
            val dictionary =
                dictionariesViewModel.dictionaryById(checkNotNull(currentCard.dictionaryId))

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
                            text = "[${(currentCard.answered * 100) / dictionary.numberOfRightAnswers}%]",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        AudioPlayerIcon(
                            ttsViewModel = ttsViewModel,
                            card = currentCard,
                            modifier = Modifier.size(64.dp),
                            size = 64.dp
                        )
                    }
                }

                Text(
                    text = currentCard.translationAsString,
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

        val enabled = !ttsViewModel.isAudioProcessing(checkNotNull(currentCard?.cardId))

        val alpha by animateFloatAsState(if (enabled) 1f else 0.5f)

        StageShowBottomToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(alpha),
            buttonsEnabled = enabled,
            onNextCardDeck = {
                onNextCard(false)
            },
            onKnown = {
                onNextCard(true)
            },
        )
    }
}

@Composable
private fun StageShowBottomToolbar(
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