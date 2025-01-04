package com.github.sszuev.flashcards.android.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.Card
import com.github.sszuev.flashcards.android.models.CardViewModel
import kotlinx.coroutines.launch

private const val FIRST_COLUMN_WIDTH = 32
private const val SECOND_COLUMN_WIDTH = 58
private const val THIRD_COLUMN_WIDTH = 10

@Composable
fun CardsScreen(
    dictionaryId: String,
    viewModel: CardViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {
    val searchQuery = remember { mutableStateOf("") }
    val cards by viewModel.cads
    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery.value) {
        val index = cards.indexOfFirst { it.word.startsWith(searchQuery.value, ignoreCase = true) }
        if (index != -1) {
            listState.animateScrollToItem(index, scrollOffset = 50)
            viewModel.selectedCardId.value = cards[index].cardId
        } else {
            viewModel.selectedCardId.value = null
        }
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
                    viewModel = viewModel,
                    dictionaryId = dictionaryId,
                    listState = listState,
                )
            }
        }
        CardsBottomToolbar(
            selectedCardId = viewModel.selectedCardId.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            searchQuery = searchQuery,
            listState = listState,
            cards = cards,
        )
    }
}

@Composable
fun CardsTable(
    viewModel: CardViewModel,
    dictionaryId: String,
    listState: LazyListState,
) {
    val cards by viewModel.cads
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    LaunchedEffect(Unit) {
        viewModel.loadCards(dictionaryId)
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

                errorMessage != null -> {
                    Text(
                        text = checkNotNull(errorMessage),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

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
                                onSelect = {
                                    viewModel.selectedCardId.value =
                                        if (viewModel.selectedCardId.value == card.cardId) {
                                            null
                                        } else {
                                            card.cardId
                                        }
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
    card: Card,
    containerWidthDp: Dp,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
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
        TableCell(
            text = card.translation,
            weight = SECOND_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp
        )
        TableCell(
            text = card.answered.toString(),
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
    cards: List<Card>,
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
            placeholder = { Text("Search...") }
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