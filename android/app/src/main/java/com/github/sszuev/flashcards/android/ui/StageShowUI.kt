package com.github.sszuev.flashcards.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.normalize
import com.github.sszuev.flashcards.android.utils.splitToTokenChunks
import com.github.sszuev.flashcards.android.utils.translationAsString


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

    val settings = checkNotNull(settingsViewModel.settings.value)

    val deckLoaded = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(dictionariesViewModel.selectedDictionaryIds.value) {
        if (!deckLoaded.value) {
            tutorViewModel.loadNextCardDeck(
                dictionaryIds = dictionariesViewModel.selectedDictionaryIds.value,
                length = settings.stageShowNumberOfWords,
                onComplete = {
                    it.firstOrNull()?.let { card ->
                        ttsViewModel.loadAndPlayAudio(card.audioId)
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

    val errorMessage = tutorViewModel.errorMessage.value
    if (!errorMessage.isNullOrBlank()) {
        ErrorMessageBox(errorMessage)
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
            ttsViewModel.loadAndPlayAudio(nextCard.audioId)
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

            var openTokenId by remember { mutableStateOf<String?>(null) }

            currentCard.examples.forEachIndexed { lineIndex, example ->
                UnderlinedTokensSingleLineWithPopup(
                    text = example.first,
                    sourceLang = dictionary.sourceLanguage,
                    targetLang = dictionary.targetLanguage,
                    cardsViewModel = cardsViewModel,
                    ttsViewModel = ttsViewModel,
                    openTokenId = openTokenId,
                    onOpenToken = { openTokenId = it },
                    onClose = { openTokenId = null },
                    lineId = "ex:$lineIndex",
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
        }

        val enabled = !ttsViewModel.isAudioProcessing(checkNotNull(currentCard).audioId)

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
            .onSizeChanged { size ->
                @Suppress("AssignedValueIsNeverRead")
                containerWidthPx = size.width
            },
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

@Composable
fun UnderlinedTokensSingleLineWithPopup(
    text: String,
    sourceLang: String,
    targetLang: String,
    cardsViewModel: CardsViewModel,
    ttsViewModel: TTSViewModel,

    openTokenId: String?,
    onOpenToken: (String) -> Unit,
    onClose: () -> Unit,

    lineId: String,
    modifier: Modifier = Modifier,
) {
    val (chunks, tail) = remember(text) {
        text.splitToTokenChunks()
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        chunks.forEachIndexed { idx, ch ->
            val tokenId = "$lineId:$idx"

            Box {
                Text(
                    text = buildAnnotatedString {
                        append(ch.leading)
                        withStyle(
                            SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium,
                            )
                        ) {
                            append(ch.tokenDisplay)
                        }
                        append(ch.attached)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.noRippleClickable {
                        if (openTokenId == tokenId) {
                            onClose()
                        } else {
                            onOpenToken(tokenId)
                            cardsViewModel.fetchCard(ch.tokenQuery, sourceLang, targetLang)
                        }
                    }
                )

                DropdownMenu(
                    expanded = openTokenId == tokenId,
                    onDismissRequest = {
                        if (openTokenId == tokenId) onClose()
                    },
                    properties = PopupProperties(focusable = false)
                ) {
                    TokenPopupItemWithFetch(
                        token = ch.tokenQuery,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                        cardsViewModel = cardsViewModel,
                        ttsViewModel = ttsViewModel,
                        isOpen = openTokenId == tokenId,
                        onClose = onClose,
                    )
                }
            }
        }

        if (tail.isNotEmpty()) {
            Text(
                text = tail,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TokenPopupItemWithFetch(
    token: String,
    sourceLang: String,
    targetLang: String,
    cardsViewModel: CardsViewModel,
    ttsViewModel: TTSViewModel,
    isOpen: Boolean,
    onClose: () -> Unit,
) {
    val activeKey by cardsViewModel.activeFetchKey
    val fetchedKey by cardsViewModel.fetchedCardKey
    val fetched by cardsViewModel.fetchedCard
    val isFetching by cardsViewModel.isCardFetching

    val key = remember(token, sourceLang, targetLang) {
        Triple(token.trim(), sourceLang, targetLang)
    }

    val isMineActive = activeKey == key

    val normalized = fetched?.normalize()
    val hasDataForThis = fetchedKey == key && normalized != null

    val finishedNoDataForThis = fetchedKey == key && !isFetching && fetched == null

    LaunchedEffect(isOpen, finishedNoDataForThis) {
        if (isOpen && finishedNoDataForThis) onClose()
    }

    var opening by remember(key) { mutableStateOf(false) }
    LaunchedEffect(isOpen) { opening = isOpen }
    LaunchedEffect(isOpen, isMineActive, isFetching, hasDataForThis) {
        if (!isOpen) opening = false
        else if (isMineActive || isFetching || hasDataForThis) opening = false
    }

    val itemColors = MenuDefaults.itemColors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
    )

    val showSpinner = isOpen && !hasDataForThis && (opening || (isMineActive && isFetching))

    when {
        hasDataForThis -> {
            val card0 = checkNotNull(normalized)

            val safeCard = remember(card0, key) {
                if (card0.cardId != null) card0
                else card0.copy(cardId = "tok:${key.first}:${key.second}:${key.third}")
            }

            DropdownMenuItem(
                enabled = false,
                colors = itemColors,
                onClick = {},
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = safeCard.word,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        AudioPlayerIcon(
                            ttsViewModel = ttsViewModel,
                            card = safeCard,
                            size = 20.dp,
                        )
                    }
                },
            )

            DropdownMenuItem(
                enabled = false,
                colors = itemColors,
                onClick = {},
                text = {
                    Text(
                        text = safeCard.translationAsString,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }

        showSpinner -> {
            DropdownMenuItem(
                enabled = false,
                colors = itemColors,
                onClick = {},
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }

        finishedNoDataForThis -> {
            DropdownMenuItem(
                enabled = false,
                colors = itemColors,
                onClick = {},
                text = { Text("No data", style = MaterialTheme.typography.bodyMedium) }
            )
        }

        else -> {
            DropdownMenuItem(
                enabled = false,
                colors = itemColors,
                onClick = {},
                text = { Text("Loading…", style = MaterialTheme.typography.bodyMedium) }
            )
        }
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}