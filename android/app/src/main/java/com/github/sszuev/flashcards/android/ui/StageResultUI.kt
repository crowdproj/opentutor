package com.github.sszuev.flashcards.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.utils.shortText


private const val FIRST_COLUMN_WIDTH = 20
private const val SECOND_COLUMN_WIDTH = 35
private const val THIRD_COLUMN_WIDTH = 27
private const val FOURTH_COLUMN_WIDTH = 18

@Composable
fun StageResultScreen(
    cardViewModel: CardViewModel,
    dictionariesViewModel: DictionaryViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {
    val cards = cardViewModel.cardsDeck.value.sortedBy { -it.answered }
    if (cards.isEmpty()) {
        return
    }
    val dictionaries =
        dictionariesViewModel.selectedDictionariesList.associateBy { it.dictionaryId }

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    BackHandler {
        onHomeClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .onSizeChanged { size -> containerWidthPx = size.width }
    ) {
        Column {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)

            Text(
                text = "Stage: results",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
                    .border(BorderStroke(1.dp, Color.Gray))
            ) {
                // Header Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .border(BorderStroke(1.dp, Color.Gray))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell(
                            text = "Word",
                            weight = FIRST_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp
                        )
                        TableCell(
                            text = "Translation",
                            weight = SECOND_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp
                        )
                        TableCell(
                            text = "Dictionary",
                            weight = THIRD_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp
                        )
                        TableCell(
                            text = "%",
                            weight = FOURTH_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp
                        )
                    }
                }

                // Data Rows
                items(cards) { card ->
                    val dictionary = checkNotNull(dictionaries[checkNotNull(card.dictionaryId)]) {
                        "no dictionary for card = ${card.cardId}"
                    }
                    val statusColor = if (card.answered >= dictionary.numberOfRightAnswers)
                        Color.Green
                    else
                        Color.Blue

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.Gray))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell(
                            text = card.word,
                            weight = FIRST_COLUMN_WIDTH,
                            textColor = statusColor,
                            containerWidthDp = containerWidthDp,
                        )
                        TableCellWithPopup(
                            shortText = shortText(card.translation),
                            fullText = card.translation,
                            weight = SECOND_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp
                        )
                        TableCell(
                            text = dictionary.name,
                            weight = THIRD_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp
                        )
                        TableCell(
                            text = "${card.answered}%",
                            weight = FOURTH_COLUMN_WIDTH,
                            containerWidthDp = containerWidthDp,
                        )
                    }
                }
            }
        }
    }
}
