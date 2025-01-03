package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private const val tag = "CardsUI"
private const val FIRST_COLUMN_WIDTH = 32
private const val SECOND_COLUMN_WIDTH = 60
private const val THIRD_COLUMN_WIDTH = 8

@Composable
fun CardsScreen(
    dictionaryId: String,
    viewModel: CardViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
                CardsTable(viewModel, dictionaryId)
            }
        }
    }
}

@Composable
fun CardsTable(
    viewModel: CardViewModel,
    dictionaryId: String,
) {
    val cards by viewModel.cads
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        Log.d(tag, "In LaunchedEffect ::: begin")
        coroutineScope.launch {
            viewModel.loadCards(dictionaryId)
        }
        Log.d(tag, "In LaunchedEffect ::: finish")
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
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(cards) { card ->
                            CardsTableRow(
                                card = card,
                                containerWidthDp = containerWidthDp,
                                isSelected = viewModel.selectedCardId.value == card.cardId,
                                onSelect = { viewModel.selectedCardId.value = card.cardId }
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
            .height(100.dp)
    ) {
        TableCell(
            text = "Word ${if (currentSortField == "word") if (isAscending) "↑" else "↓" else ""}",
            weight = FIRST_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("word") }
        )
        TableCell(
            text = "Translation ${if (currentSortField == "translation") if (isAscending) "↑" else "↓" else ""}",
            weight = SECOND_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("translation") }
        )
        TableCell(
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