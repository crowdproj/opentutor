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
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
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
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNextStage: () -> Unit = {},
    direction: Boolean = true,
) {
    Log.d(tag, "StageWriting")
    if (cardViewModel.cardsDeck.value.isEmpty()) {
        onNextStage()
        return
    }
    BackHandler {
        onHomeClick()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
            Text(
                text = "Stage: writing [${if (direction) "source -> target" else "target -> source"}]",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            WritingPanels(
                cardViewModel = cardViewModel,
                dictionaryViewModel = dictionaryViewModel,
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
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    onNextStage: () -> Unit,
    direct: Boolean,
) {
    val settings = checkNotNull(settingsViewModel.settings.value) { "no settings" }
    val cards = remember {
        cardViewModel.unknownDeckCards { id ->
            dictionaryViewModel.dictionaryById(id).numberOfRightAnswers
        }.shuffled().take(settings.numberOfWordsPerStage).toMutableList()
    }

    if (cards.isEmpty()) {
        onNextStage()
        return
    }

    val errorMessage = cardViewModel.errorMessage.value
    ErrorMessageBox(errorMessage)
    if (errorMessage != null) {
        return
    }

    val currentCard = remember { mutableStateOf(cards.firstOrNull()) }
    var isEditable by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }

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

    fun onNextCard() {
        val card = cards[0]
        currentCard.value = card

        cards.removeAt(0)
        currentCard.value = cards.firstOrNull()

        inputText = ""
        isEditable = true

        if (cards.isEmpty()) {
            onNextStage()
        }
    }

    fun checkAnswer(userInput: String, card: CardEntity): Boolean {
        val expected = if (direct) {
            card.translation
        } else {
            wordAsList(card.word)
        }
        val res = correctAnswerIndexOf(expected, userInput)
        return if (res == -1) {
            Log.i(tag, "Answer is incorrect. input: $userInput, translations: $expected")
            cardViewModel.markDeckCardAsWrong(checkNotNull(card.cardId))
            false
        } else {
            Log.i(tag, "Answer is correct. input: $userInput, translations: $expected")
            val cardId = checkNotNull(card.cardId)
            val dictionary = dictionaryViewModel.dictionaryById(checkNotNull(card.dictionaryId))
            cardViewModel.updateDeckCard(
                cardId,
                dictionary.numberOfRightAnswers
            )
            true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            val card = currentCard.value
            if (card == null) {
                onNextStage()
                return@item
            }
            val dictionary =
                dictionaryViewModel.dictionaryById(checkNotNull(card.dictionaryId))

            if (direct || isTextShort(card.translationAsString)) {
                Text(
                    text = if (direct) card.word else card.translationAsString,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    onValueChange = { it: String ->
                        inputText = it
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    readOnly = !isEditable,
                    enabled = true,
                    singleLine = false,
                    maxLines = 4
                )
            } else {
                BasicText(
                    text = buildAnnotatedString {
                        val card = checkNotNull(currentCard.value)
                        val expected = if (direct) {
                            card.translation
                        } else {
                            wordAsList(card.word)
                        }
                        val correctIndex =
                            correctAnswerIndexOf(expected, inputText)

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
                            if (index != expected.lastIndex) {
                                append(", ")
                            }
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

            var buttonsEnabled by remember { mutableStateOf(false) }

            val alpha by animateFloatAsState(if (buttonsEnabled) 1f else 0.5f)

            LaunchedEffect(currentCard) {
                Log.d(tag, "DELAY")
                buttonsEnabled = false
                delay(STAGE_WRITING_BUTTONS_DELAY_MS)
                buttonsEnabled = true
            }

            Box {
                StageWritingBottomToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .alpha(alpha),
                    buttonsEnabled = buttonsEnabled && inputText.isNotBlank(),
                    isEditable = isEditable,
                    onTest = {
                        val card = checkNotNull(currentCard.value)
                        isCorrect = checkAnswer(inputText, card)
                        isEditable = false

                        buttonsEnabled = false
                        cardViewModel.viewModelScope.launch {
                            delay(STAGE_WRITING_BUTTONS_DELAY_MS)
                            buttonsEnabled = true
                            Log.d(tag, "Delay over: enabling buttons")
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
