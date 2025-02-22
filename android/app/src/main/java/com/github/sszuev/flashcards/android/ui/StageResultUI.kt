package com.github.sszuev.flashcards.android.ui

import android.util.Log
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString


private const val FIRST_COLUMN_WIDTH = 20
private const val SECOND_COLUMN_WIDTH = 35
private const val THIRD_COLUMN_WIDTH = 25
private const val FOURTH_COLUMN_WIDTH = 20

private const val tag = "StageResultUI"

@Composable
fun StageResultScreen(
    cardViewModel: CardViewModel,
    dictionaryViewModel: DictionaryViewModel,
    onHomeClick: () -> Unit = {},
) {
    Log.d(tag, "StageResult")
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    fun dictionaryNumberOfRightAnswers(card: CardEntity): Int {
        return dictionaryViewModel.dictionaryById(checkNotNull(card.dictionaryId)).numberOfRightAnswers
    }

    fun dictionaryName(card: CardEntity): String {
        return dictionaryViewModel.dictionaryById(checkNotNull(card.dictionaryId)).name
    }

    val greenCards = cardViewModel.greenDeckCards { dictionaryNumberOfRightAnswers(it) }
    val blueCards = cardViewModel.blueDeckCards { dictionaryNumberOfRightAnswers(it) }
    val redCards = cardViewModel.redDeckCards()

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

                items(greenCards) { card ->
                    CardItemRow(
                        card = card,
                        statusColor = Color.Green,
                        containerWidthDp = containerWidthDp,
                        dictionaryName = {
                            dictionaryName(it)
                        },
                        dictionaryNumberOfRightAnswers = { dictionaryNumberOfRightAnswers(it) },
                    )
                }
                items(blueCards) { card ->
                    CardItemRow(
                        card = card,
                        statusColor = Color.Blue,
                        containerWidthDp = containerWidthDp,
                        dictionaryName = {
                            dictionaryName(it)
                        },
                        dictionaryNumberOfRightAnswers = { dictionaryNumberOfRightAnswers(it) },
                    )
                }
                items(redCards) { card ->
                    CardItemRow(
                        card = card,
                        statusColor = Color.Red,
                        containerWidthDp = containerWidthDp,
                        dictionaryName = {
                            dictionaryName(it)
                        },
                        dictionaryNumberOfRightAnswers = { dictionaryNumberOfRightAnswers(it) },
                    )
                }
            }
        }
    }
}

@Composable
fun CardItemRow(
    card: CardEntity,
    statusColor: Color,
    containerWidthDp: Dp,
    dictionaryName: (CardEntity) -> String,
    dictionaryNumberOfRightAnswers: (CardEntity) -> Int,
) {
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
            shortText = shortText(card.translationAsString),
            fullText = card.translationAsString,
            weight = SECOND_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp
        )
        TableCell(
            text = dictionaryName(card),
            weight = THIRD_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp
        )
        TableCell(
            text = "${100 * card.answered / dictionaryNumberOfRightAnswers(card)}",
            weight = FOURTH_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
        )
    }
}
