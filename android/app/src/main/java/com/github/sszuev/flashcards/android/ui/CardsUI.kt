package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.entities.DictionaryEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.utils.audioResource
import com.github.sszuev.flashcards.android.utils.examplesAsList
import com.github.sszuev.flashcards.android.utils.examplesAsString
import com.github.sszuev.flashcards.android.utils.isTextShort
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString
import com.github.sszuev.flashcards.android.utils.translationFromString
import kotlinx.coroutines.launch

private const val FIRST_COLUMN_WIDTH = 32
private const val SECOND_COLUMN_WIDTH = 54
private const val THIRD_COLUMN_WIDTH = 14

private const val tag = "CardsUI"

@Composable
fun CardsScreen(
    dictionary: DictionaryEntity,
    cardViewModel: CardViewModel,
    ttsViewModel: TTSViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {
    val searchQuery = remember { mutableStateOf("") }
    val cards by cardViewModel.cards
    val listState = rememberLazyListState()
    val isEditPopupOpen = remember { mutableStateOf(false) }
    val isAddPopupOpen = remember { mutableStateOf(false) }
    val isDeletePopupOpen = remember { mutableStateOf(false) }
    val isResetPopupOpen = remember { mutableStateOf(false) }
    val selectedDictionaryIds = cardViewModel.selectedCardId
    val selectedCard = cardViewModel.selectedCard

    var previousCardSize by remember { mutableIntStateOf(cards.size) }

    LaunchedEffect(searchQuery.value, cards.size) {
        if (searchQuery.value.isNotBlank()) {
            val index =
                cards.indexOfFirst { it.word.startsWith(searchQuery.value, ignoreCase = true) }
            if (index != -1) {
                listState.animateScrollToItem(index, scrollOffset = 50)
                cardViewModel.selectCard(cards[index].cardId)
            } else {
                cardViewModel.selectCard(null)
            }
        } else if (previousCardSize > 0 && cards.size > previousCardSize) {
            listState.animateScrollToItem(cards.size - 1)
            cardViewModel.selectCard(cards.last().cardId)
        }

        previousCardSize = cards.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
                CardsTable(
                    viewModel = cardViewModel,
                    dictionaryId = checkNotNull(dictionary.dictionaryId),
                    numberOfRightAnswers = dictionary.numberOfRightAnswers,
                    listState = listState,
                )
            }
        }
        CardsBottomToolbar(
            selectedCardId = cardViewModel.selectedCardId.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            searchQuery = searchQuery,
            listState = listState,
            cards = cards,
            onEditClick = {
                if (selectedDictionaryIds.value != null) {
                    isEditPopupOpen.value = true
                }
            },
            onAddClick = {
                cardViewModel.clearFetchedCard()
                isAddPopupOpen.value = true
            },
            onDeleteClick = {
                isDeletePopupOpen.value = true
            },
            onResetClick = {
                isResetPopupOpen.value = true
            }
        )
    }
    if (isEditPopupOpen.value && selectedCard != null) {
        EditCardDialog(
            lang = dictionary.sourceLanguage,
            onSave = {
                cardViewModel.updateCard(it)
            },
            onDismiss = { isEditPopupOpen.value = false },
            card = selectedCard,
            ttsViewModel = ttsViewModel,
        )
    }
    if (isAddPopupOpen.value) {
        AddCardDialog(
            initialWord = searchQuery.value,
            dictionaryId = checkNotNull(dictionary.dictionaryId),
            sourceLang = dictionary.sourceLanguage,
            targetLang = dictionary.targetLanguage,
            onDismiss = { isAddPopupOpen.value = false },
            onSave = {
                cardViewModel.createCard(it)
            },
            viewModel = cardViewModel,
        )
    }
    if (isDeletePopupOpen.value && selectedCard != null) {
        DeleteCardDialog(
            cardName = selectedCard.word,
            onClose = { isDeletePopupOpen.value = false },
            onConfirm = {
                cardViewModel.deleteCard(checkNotNull(selectedCard.cardId))
            }
        )
    }
    if (isResetPopupOpen.value && selectedCard != null) {
        ResetCardDialog(
            cardName = selectedCard.word,
            onClose = { isResetPopupOpen.value = false },
            onConfirm = {
                cardViewModel.resetCard(checkNotNull(selectedCard.cardId))
            }
        )
    }
}

@Composable
fun CardsTable(
    viewModel: CardViewModel,
    dictionaryId: String,
    numberOfRightAnswers: Int,
    listState: LazyListState,
) {
    val cards by viewModel.cards
    val isLoading by viewModel.isCardsLoading
    val isLoaded = rememberSaveable { mutableStateOf(false) }
    val errorMessage by viewModel.errorMessage

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    LaunchedEffect(Unit) {
        if (!isLoaded.value) {
            viewModel.loadCards(dictionaryId)
            isLoaded.value = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size -> containerWidthPx = size.width }
    ) {
        if (containerWidthDp <= 0.dp) {
            return@Box
        }
        Column {

            CardsTableHeader(
                containerWidthDp = containerWidthDp,
                onSort = { viewModel.sortBy(it) },
                currentSortField = viewModel.sortField.value,
                isAscending = viewModel.isAscending.value,
            )

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                errorMessage != null -> ErrorMessageBox(errorMessage)

                cards.isEmpty() -> {
                    Text(
                        text = "No cards found",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .padding(bottom = 165.dp)
                    ) {
                        items(cards) { card ->
                            CardsTableRow(
                                card = card,
                                containerWidthDp = containerWidthDp,
                                isSelected = viewModel.selectedCardId.value == card.cardId,
                                numberOfRightAnswers = numberOfRightAnswers,
                                onSelect = {
                                    viewModel.selectCard(
                                        if (viewModel.selectedCardId.value == card.cardId) {
                                            null
                                        } else {
                                            card.cardId
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

        }
    }

}

@Composable
fun CardsTableHeader(
    containerWidthDp: Dp,
    onSort: (String) -> Unit,
    currentSortField: String?,
    isAscending: Boolean
) {
    Row(
        modifier = Modifier
            .background(Color.LightGray)
            .border(1.dp, Color.Black)
            .height(60.dp)
    ) {
        HeaderTableCell(
            text = "Word ${if (currentSortField == "word") if (isAscending) "↑" else "↓" else ""}",
            weight = FIRST_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("word") }
        )
        HeaderTableCell(
            text = "Translation ${if (currentSortField == "translation") if (isAscending) "↑" else "↓" else ""}",
            weight = SECOND_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("translation") }
        )
        HeaderTableCell(
            text = "% ${if (currentSortField == "status") if (isAscending) "↑" else "↓" else ""}",
            weight = THIRD_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("status") }
        )
    }
}

@Composable
fun CardsTableRow(
    card: CardEntity,
    numberOfRightAnswers: Int,
    containerWidthDp: Dp,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val percentage =
        (100.0 * card.answered / numberOfRightAnswers).takeIf { it < 100 }?.toInt() ?: 100
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
            .background(if (isSelected) Color.Green else Color.Transparent)
            .clickable {
                onSelect()
            }
    ) {
        TableCell(
            text = card.word,
            weight = FIRST_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp
        )
        if (isTextShort(card.translationAsString)) {
            TableCell(
                text = card.translationAsString,
                weight = SECOND_COLUMN_WIDTH,
                containerWidthDp = containerWidthDp
            )
        } else {
            TableCellWithPopup(
                shortText = shortText(card.translationAsString),
                fullText = card.translationAsString,
                weight = SECOND_COLUMN_WIDTH,
                containerWidthDp = containerWidthDp,
                onShortClick = onSelect,
            )
        }
        TableCell(
            text = percentage.toString(),
            weight = THIRD_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp
        )
    }
}

@Composable
fun CardsBottomToolbar(
    modifier: Modifier = Modifier,
    searchQuery: MutableState<String>,
    listState: LazyListState,
    cards: List<CardEntity>,
    selectedCardId: String?,
    onAddClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onResetClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .imePadding()
            .onSizeChanged { size -> containerWidthPx = size.width },
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = searchQuery.value,
            onValueChange = { query ->
                searchQuery.value = query
                coroutineScope.launch {
                    val index =
                        cards.indexOfFirst { it.word.startsWith(query, ignoreCase = true) }
                    if (index != -1) {
                        listState.animateScrollToItem(index)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Type...") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                label = "ADD",
                containerWidthDp = containerWidthDp,
                weight = 42.86f,
                onClick = onAddClick
            )
            ToolbarButton(
                label = "EDIT",
                containerWidthDp = containerWidthDp,
                weight = 57.14f,
                onClick = onEditClick,
                enabled = selectedCardId != null,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                label = "RESET",
                containerWidthDp = containerWidthDp,
                weight = 45.45f,
                onClick = onResetClick,
                enabled = selectedCardId != null,
            )
            ToolbarButton(
                label = "DELETE",
                containerWidthDp = containerWidthDp,
                weight = 54.55f,
                onClick = onDeleteClick,
                enabled = selectedCardId != null,
            )
        }
    }
}

@Composable
fun EditCardDialog(
    lang: String,
    card: CardEntity,
    ttsViewModel: TTSViewModel,
    onDismiss: () -> Unit,
    onSave: (CardEntity) -> Unit,
) {
    var word by remember { mutableStateOf(card.word) }
    var translation by remember { mutableStateOf(card.translationAsString) }
    var examples by remember { mutableStateOf(examplesAsString(card.examples)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 600.dp)
                    .imePadding(),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = word,
                            onValueChange = { word = it },
                            label = { Text("Word") },
                            modifier = Modifier.weight(1f)
                        )
                        AudioPlayerIcon(ttsViewModel, card)
                    }
                }

                item {
                    OutlinedTextField(
                        value = translation,
                        onValueChange = { translation = it },
                        label = { Text("Translation") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                item {
                    Text(
                        text = "Examples",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    BasicTextField(
                        value = examples,
                        onValueChange = { examples = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .heightIn(min = 100.dp, max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                MaterialTheme.shapes.small
                            )
                            .padding(8.dp),
                        maxLines = Int.MAX_VALUE
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onDismiss) {
                            Text("CLOSE")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = word.isNotBlank() && translation.isNotBlank(),
                            onClick = {
                                val sound = if (card.word == word) {
                                    card.audioId
                                } else {
                                    ttsViewModel.invalidate(checkNotNull(card.cardId))
                                    audioResource(lang, word)
                                }
                                onSave(
                                    card.copy(
                                        word = word,
                                        translation = translationFromString(translation),
                                        examples = examplesAsList(examples),
                                        audioId = sound,
                                    )
                                )
                                onDismiss()
                            }) {
                            Text("SAVE")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCardDialog(
    initialWord: String,
    dictionaryId: String,
    sourceLang: String,
    targetLang: String,
    onDismiss: () -> Unit,
    onSave: (CardEntity) -> Unit,
    viewModel: CardViewModel,
) {
    var word by remember { mutableStateOf(initialWord) }
    var translation by remember { mutableStateOf("") }
    var examples by remember { mutableStateOf("") }

    LaunchedEffect(initialWord) {
        viewModel.fetchCard(initialWord, sourceLang, targetLang)
    }

    val fetchedCard = viewModel.fetchedCard.value
    LaunchedEffect(fetchedCard) {
        fetchedCard?.let { fetched ->
            if (word == initialWord) word = fetched.word
            if (translation.isBlank()) translation = fetched.translationAsString
            if (examples.isBlank()) examples = examplesAsString(fetched.examples)
        }
    }

    Log.d(tag, "Fetch result: [word='$word', translation='$translation', examples='$examples']")
    if (word.isBlank()) {
        word = initialWord
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 600.dp)
                    .imePadding(),
            ) {
                if (viewModel.isCardFetching.value) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        label = { Text("Word") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        enabled = !viewModel.isCardFetching.value,
                    )
                }

                item {
                    OutlinedTextField(
                        value = translation,
                        onValueChange = { translation = it },
                        label = { Text("Translation") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        enabled = !viewModel.isCardFetching.value,
                    )
                }

                item {
                    Text(
                        text = "Examples",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    BasicTextField(
                        value = examples,
                        onValueChange = { examples = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .heightIn(min = 100.dp, max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                MaterialTheme.shapes.small
                            )
                            .padding(8.dp),
                        maxLines = Int.MAX_VALUE,
                        enabled = !viewModel.isCardFetching.value,
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onDismiss) {
                            Text("CLOSE")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = word.isNotBlank() && translation.isNotBlank(),
                            onClick = {
                                onSave(
                                    CardEntity(
                                        cardId = null,
                                        dictionaryId = dictionaryId,
                                        word = word,
                                        translation = translationFromString(translation),
                                        examples = examplesAsList(examples),
                                        answered = 0,
                                        audioId = audioResource(sourceLang, word)
                                    )
                                )
                                onDismiss()
                            }) {
                            Text("SAVE")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DeleteCardDialog(
    cardName: String,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("DELETE:") },
        text = { Text(cardName) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onClose()
            }) {
                Text("CONFIRM")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("CLOSE")
            }
        }
    )
}

@Composable
fun ResetCardDialog(
    cardName: String,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("RESET:") },
        text = { Text(cardName) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onClose()
            }) {
                Text("CONFIRM")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("CLOSE")
            }
        }
    )
}