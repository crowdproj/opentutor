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
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString


private const val FIRST_COLUMN_WIDTH = 32
private const val SECOND_COLUMN_WIDTH = 54
private const val THIRD_COLUMN_WIDTH = 14

private const val tag = "StageResultUI"

@Composable
fun StageResultScreen(
    tutorViewModel: TutorViewModel,
    dictionariesViewModel: DictionariesViewModel,
    onHomeClick: () -> Unit = {},
) {
    Log.d(tag, "StageResult")
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    fun dictionaryNumberOfRightAnswers(card: CardEntity): Int {
        return dictionariesViewModel.dictionaryById(checkNotNull(card.dictionaryId)).numberOfRightAnswers
    }

    val greenCards = tutorViewModel.greenDeckCards { dictionaryNumberOfRightAnswers(it) }
    val blueCards = tutorViewModel.blueDeckCards { dictionaryNumberOfRightAnswers(it) }
    val redCards = tutorViewModel.redDeckCards()

    BackHandler {
        onHomeClick()
    }

    val errorMessage = tutorViewModel.errorMessage.value
    if (!errorMessage.isNullOrBlank()) {
        ErrorMessageBox(errorMessage)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size -> containerWidthPx = size.width }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .border(BorderStroke(1.dp, Color.Gray))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderTableCell(
                    text = "WORD",
                    weight = FIRST_COLUMN_WIDTH,
                    containerWidthDp = containerWidthDp
                )
                HeaderTableCell(
                    text = "TRANSLATION",
                    weight = SECOND_COLUMN_WIDTH,
                    containerWidthDp = containerWidthDp
                )
                HeaderTableCell(
                    text = "%",
                    weight = THIRD_COLUMN_WIDTH,
                    containerWidthDp = containerWidthDp
                )
            }

            FadeLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
                    .border(BorderStroke(1.dp, Color.Gray))
            ) {

                items(greenCards) { card ->
                    CardItemRow(
                        card = card,
                        statusColor = Color.Green,
                        containerWidthDp = containerWidthDp,
                        dictionaryNumberOfRightAnswers = { dictionaryNumberOfRightAnswers(it) },
                    )
                }
                items(blueCards) { card ->
                    CardItemRow(
                        card = card,
                        statusColor = Color.Blue,
                        containerWidthDp = containerWidthDp,
                        dictionaryNumberOfRightAnswers = { dictionaryNumberOfRightAnswers(it) },
                    )
                }
                items(redCards) { card ->
                    CardItemRow(
                        card = card,
                        statusColor = Color.Red,
                        containerWidthDp = containerWidthDp,
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
    dictionaryNumberOfRightAnswers: (CardEntity) -> Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.Gray)),
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
            text = "${100 * card.answered / dictionaryNumberOfRightAnswers(card)}",
            weight = THIRD_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
        )
    }
}
