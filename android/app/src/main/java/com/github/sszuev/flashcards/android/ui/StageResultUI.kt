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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel
import com.github.sszuev.flashcards.android.utils.shortText
import com.github.sszuev.flashcards.android.utils.translationAsString


private const val FIRST_COLUMN_WIDTH = 32
private const val SECOND_COLUMN_WIDTH = 50
private const val THIRD_COLUMN_WIDTH = 18

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
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    containerWidthDp = containerWidthDp
                )
                HeaderTableCell(
                    text = "TRANSLATION",
                    weight = SECOND_COLUMN_WIDTH,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    containerWidthDp = containerWidthDp
                )
                HeaderTableCell(
                    text = "%",
                    weight = THIRD_COLUMN_WIDTH,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    containerWidthDp = containerWidthDp
                )
            }

            HorizontalDivider(
                thickness = 5.dp,
                color = Color(0xFFDDDDDD),
                modifier = Modifier
            )

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.Gray))
                .padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            TableCellWithContent(
                weight = FIRST_COLUMN_WIDTH,
                containerWidthDp = containerWidthDp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(statusColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TableCellText(
                        text = card.word,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 40.sp,
                        textColor = Color.DarkGray,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }

            TableCellWithPopup(
                shortText = shortText(card.translationAsString),
                fullText = card.translationAsString,
                weight = SECOND_COLUMN_WIDTH,
                fontWeight = FontWeight.Normal,
                containerWidthDp = containerWidthDp
            )
            TableCell(
                text = "${(100.0 * card.answered / dictionaryNumberOfRightAnswers(card)).toInt()}",
                weight = THIRD_COLUMN_WIDTH,
                containerWidthDp = containerWidthDp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Right,
            )
        }
    }
}
