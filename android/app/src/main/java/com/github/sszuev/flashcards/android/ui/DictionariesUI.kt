package com.github.sszuev.flashcards.android.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sszuev.flashcards.android.Dictionary
import com.github.sszuev.flashcards.android.getUsernameFromPreferences
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import kotlinx.coroutines.launch

private const val tag = "DictionariesUI"

@Composable
fun MainDictionariesScreen(
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    viewModel: DictionaryViewModel,
) {
    Column {
        TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
        DictionaryTable(
            viewModel = viewModel,
        )
    }
}

@Composable
fun TopBar(
    onSignOut: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val username = getUsernameFromPreferences(LocalContext.current as Activity)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Absolute.Right,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[ $username ]",
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = "home",
            color = Color.Blue,
            modifier = Modifier
                .clickable { onHomeClick() }
                .padding(end = 16.dp)
        )
        Text(
            text = "sign out",
            color = Color.Blue,
            modifier = Modifier
                .clickable { onSignOut() }
        )
    }
}

@Composable
fun DictionaryTable(
    viewModel: DictionaryViewModel,
) {
    val dictionaries by viewModel.dictionaries
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        Log.d(tag, "In LaunchedEffect ::: begin")
        coroutineScope.launch {
            viewModel.loadDictionaries()
        }
        Log.d(tag, "In LaunchedEffect ::: finish")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size -> containerWidthPx = size.width }
    ) {
        if (containerWidthDp > 0.dp) {
            Column {
                TableHeader(containerWidthDp)

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

                    dictionaries.isEmpty() -> {
                        Text(
                            text = "No dictionaries found",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> {
                        dictionaries.forEach { row ->
                            TableRow(row, containerWidthDp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeader(containerWidthDp: Dp) {
    Row(
        modifier = Modifier
            .background(Color.LightGray)
            .border(1.dp, Color.Black)
            .height(100.dp)
    ) {
        TableRow(
            first = "Dictionary name",
            second = "Source language",
            third = "Target language",
            fourth = "Total number of words",
            fifth = "Number of learned words",
            containerWidthDp = containerWidthDp,
        )
    }
}

@Composable
fun TableRow(row: Dictionary, containerWidthDp: Dp) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.Black)
    ) {
        TableRow(
            first = row.name,
            second = row.sourceLanguage,
            third = row.targetLanguage,
            fourth = row.totalWords.toString(),
            fifth = row.learnedWords.toString(),
            containerWidthDp = containerWidthDp,
        )
    }
}

@Composable
fun TableRow(
    first: String,
    second: String,
    third: String,
    fourth: String,
    fifth: String,
    containerWidthDp: Dp
) {
    TableCell(text = first, weight = 28, containerWidthDp = containerWidthDp)
    TableCell(text = second, weight = 19, containerWidthDp = containerWidthDp)
    TableCell(text = third, weight = 19, containerWidthDp = containerWidthDp)
    TableCell(text = fourth, weight = 16, containerWidthDp = containerWidthDp)
    TableCell(text = fifth, weight = 18, containerWidthDp = containerWidthDp)
}

@Composable
fun TableCell(text: String, weight: Int, containerWidthDp: Dp) {
    Box(
        modifier = Modifier
            .width(((containerWidthDp * weight) / 100f))
            .padding(4.dp),

        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Start,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            softWrap = true,
            lineHeight = 20.sp,
        )
    }

}