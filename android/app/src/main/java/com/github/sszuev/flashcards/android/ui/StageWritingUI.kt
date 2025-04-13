package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.github.sszuev.flashcards.android.STAGE_WRITING_BUTTONS_DELAY_MS
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.correctAnswerIndexOf
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString
import com.github.sszuev.flashcards.android.utils.wordAsList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "StageWritingUI"

@Composable
fun StageWritingScreen(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageWriting")
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
                text = "Stage: writing [${if (direction) "source -> target" else "target -> source"}]",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            WritingPanels(
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
fun WritingPanels(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onNextStage: () -> Unit,
    direct: Boolean,
) {
    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }

    // save cardIds and currentIndex
    val cardIds = rememberSaveable {
        tutorViewModel.unknownDeckCards { id ->
            dictionariesViewModel.dictionaryById(id).numberOfRightAnswers
        }.shuffled().take(settings.numberOfWordsPerStage).mapNotNull { it.cardId }
    }

    val cards = cardIds.mapNotNull { id ->
        tutorViewModel.cardsDeck.value.find { it.cardId == id }
    }

    val currentIndex = rememberSaveable { mutableIntStateOf(0) }

    val card = cards.getOrNull(currentIndex.intValue)
    if (card == null) {
        onNextStage()
        return
    }

    val errorMessage = tutorViewModel.errorMessage.value
    if (errorMessage != null) {
        Log.e(tag, errorMessage)
        return
    }

    var isEditable by rememberSaveable { mutableStateOf(true) }
    var inputText by rememberSaveable { mutableStateOf("") }
    var isCorrect by rememberSaveable { mutableStateOf(false) }

    val hasPlayedAudio = rememberSaveable(card.cardId) { mutableStateOf(false) }
    if (direct && !hasPlayedAudio.value) {
        LaunchedEffect(card.cardId) {
            Log.d(tag, "Playing audio for: ${card.word}")
            ttsViewModel.loadAndPlayAudio(card)
            hasPlayedAudio.value = true
        }
    }

    fun checkAnswer(userInput: String): Boolean {
        val expected = if (direct) card.translation else wordAsList(card.word)
        val res = correctAnswerIndexOf(expected, userInput)
        return if (res == -1) {
            tutorViewModel.markDeckCardAsWrong(checkNotNull(card.cardId))
            false
        } else {
            val cardId = checkNotNull(card.cardId)
            val dictionary = dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId))
            tutorViewModel.updateDeckCard(
                cardId = cardId,
                numberOfRightAnswers = dictionary.numberOfRightAnswers,
                updateCard = { cardsViewModel.updateCard(it) }
            )
            true
        }
    }

    fun onNextCard() {
        currentIndex.intValue++
        isEditable = true
        inputText = ""
        if (currentIndex.intValue >= cards.size) {
            onNextStage()
        }
    }

    // UI
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val dictionary = dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId))

            if (direct || isTextShort(card.translationAsString)) {
                Text(
                    text = if (direct) card.word else card.translationAsString,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                val txt = card.translationAsString
                TextWithPopup(
                    shortText = shortText(txt),
                    fullText = txt,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
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

        item {
            if (isEditable) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    maxLines = 4
                )
            } else {
                BasicText(
                    text = buildAnnotatedString {
                        val expected = if (direct) card.translation else wordAsList(card.word)
                        val correctIndex = correctAnswerIndexOf(expected, inputText)

                        withStyle(
                            style = if (correctIndex != -1) {
                                SpanStyle(color = Color.Green)
                            } else {
                                SpanStyle(
                                    color = Color.Red,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        ) {
                            append(inputText.trim())
                        }

                        append(" — ")

                        expected.forEachIndexed { index, ex ->
                            if (index == correctIndex) {
                                withStyle(style = SpanStyle(color = Color.Green)) {
                                    append(ex)
                                }
                            } else {
                                append(ex)
                            }
                            if (index != expected.lastIndex) append(", ")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        item {
            var buttonsEnabled by rememberSaveable { mutableStateOf(false) }
            val alpha by animateFloatAsState(if (buttonsEnabled) 1f else 0.5f)

            val lastCardId = rememberSaveable { mutableStateOf<String?>(null) }
            val cardId = card.cardId

            LaunchedEffect(cardId) {
                if (cardId != null && cardId != lastCardId.value) {
                    lastCardId.value = cardId
                    Log.d(tag, "DELAY")
                    buttonsEnabled = false
                    delay(STAGE_WRITING_BUTTONS_DELAY_MS)
                    buttonsEnabled = true
                }
            }

            Box {
                StageWritingBottomToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .alpha(alpha),
                    buttonsEnabled = buttonsEnabled && inputText.isNotBlank(),
                    isEditable = isEditable,
                    onTest = {
                        isCorrect = checkAnswer(inputText)
                        isEditable = false
                        buttonsEnabled = false
                        tutorViewModel.viewModelScope.launch {
                            delay(STAGE_WRITING_BUTTONS_DELAY_MS)
                            buttonsEnabled = true
                        }
                        if (!direct) {
                            ttsViewModel.loadAndPlayAudio(card)
                        }
                    },
                    onNext = {
                        onNextCard()
                    }
                )
            }
        }
    }
}

@Composable
private fun StageWritingBottomToolbar(
    modifier: Modifier = Modifier,
    buttonsEnabled: Boolean,
    isEditable: Boolean,
    onNext: () -> Unit = {},
    onTest: () -> Unit = {},
) {
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                if (isEditable) {
                    ToolbarButton(
                        label = "TEST",
                        containerWidthDp = containerWidthDp,
                        weight = 100.0f,
                        onClick = onTest,
                        enabled = buttonsEnabled,
                    )
                } else {
                    ToolbarButton(
                        label = "NEXT",
                        containerWidthDp = containerWidthDp,
                        weight = 100.0f,
                        onClick = onNext,
                        enabled = buttonsEnabled,
                    )
                }
            }
        }
    }
}
