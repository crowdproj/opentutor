package com.github.sszuev.flashcards.android.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.sszuev.flashcards.android.Dictionary
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import kotlinx.coroutines.launch

private const val tag = "DictionariesUI"

@Composable
fun DictionariesScreen(
    navController: NavHostController,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    viewModel: DictionaryViewModel,
) {
    val selectedDictionaryIds = viewModel.selectedDictionaryIds
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
            DictionaryTable(
                viewModel = viewModel,
            )
        }
        BottomToolbar(
            onCardsClick = {
                if (selectedDictionaryIds.size == 1) {
                    val dictionaryId = selectedDictionaryIds.first()
                    Log.i(tag, "Go to cards/$dictionaryId")
                    navController.navigate("cards/$dictionaryId")
                }
            },
            selectedDictionaryIds = selectedDictionaryIds,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@SuppressLint("ReturnFromAwaitPointerEventScope")
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
        if (containerWidthDp <= 0.dp) {
            return@Box
        }
        Column {
            DictionariesTableHeader(
                containerWidthDp = containerWidthDp,
                onSort = { viewModel.sortBy(it) },
                currentSortField = viewModel.sortField.value,
                isAscending = viewModel.isAscending.value
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

                dictionaries.isEmpty() -> {
                    Text(
                        text = "No dictionaries found",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    dictionaries.forEach { dictionary ->
                        val isSelected =
                            viewModel.selectedDictionaryIds.contains(dictionary.dictionaryId)
                        DictionariesTableRow(
                            dictionary = dictionary,
                            containerWidthDp = containerWidthDp,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.toggleSelection(
                                    dictionaryId = dictionary.dictionaryId,
                                    isSelected = it,
                                )
                            }
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun DictionariesTableHeader(
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
            text = "Dictionary name ${if (currentSortField == "name") if (isAscending) "↑" else "↓" else ""}",
            weight = 28,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("name") }
        )
        TableCell(
            text = "Source language ${if (currentSortField == "sourceLanguage") if (isAscending) "↑" else "↓" else ""}",
            weight = 19,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("sourceLanguage") }
        )
        TableCell(
            text = "Target language ${if (currentSortField == "targetLanguage") if (isAscending) "↑" else "↓" else ""}",
            weight = 19,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("targetLanguage") }
        )
        TableCell(
            text = "Total number of words ${if (currentSortField == "totalWords") if (isAscending) "↑" else "↓" else ""}",
            weight = 16,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("totalWords") }
        )
        TableCell(
            text = "Number of learned words ${if (currentSortField == "learnedWords") if (isAscending) "↑" else "↓" else ""}",
            weight = 18,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("learnedWords") }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DictionariesTableRow(
    dictionary: Dictionary,
    containerWidthDp: Dp,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
            .background(if (isSelected) Color.Green else Color.Transparent)
            .combinedClickable(
                onClick = {
                    onSelect(!isSelected)
                },
                onLongClick = {
                    onSelect(true)
                }
            )
    ) {
        DictionariesTableRow(
            first = dictionary.name,
            second = dictionary.sourceLanguage,
            third = dictionary.targetLanguage,
            fourth = dictionary.totalWords.toString(),
            fifth = dictionary.learnedWords.toString(),
            containerWidthDp = containerWidthDp,
        )
    }
}

@Composable
fun DictionariesTableRow(
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
fun BottomToolbar(
    modifier: Modifier = Modifier,
    selectedDictionaryIds: Set<String>,
    onRunClick: () -> Unit = {},
    onCardsClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .padding(8.dp)
            .onSizeChanged { size -> containerWidthPx = size.width },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(
            label = "RUN",
            containerWidthDp = containerWidthDp,
            weight = 9.375f,
            onClick = onRunClick,
        )
        ToolbarButton(
            label = "CARDS",
            containerWidthDp = containerWidthDp,
            weight = 15.625f,
            onClick = onCardsClick,
            enabled = selectedDictionaryIds.size == 1,
        )
        ToolbarButton(
            label = "CREATE",
            containerWidthDp = containerWidthDp,
            weight = 18.75f,
            onClick = onCreateClick,
        )
        ToolbarButton(
            label = "EDIT",
            containerWidthDp = containerWidthDp,
            weight = 12.5f,
            onClick = onEditClick,
        )
        ToolbarButton(
            label = "DELETE",
            containerWidthDp = containerWidthDp,
            weight = 18.75f,
            onClick = onDeleteClick,
        )
        ToolbarButton(
            label = "SETTINGS",
            containerWidthDp = containerWidthDp,
            weight = 25f,
            onClick = onSettingsClick,
        )
    }
}

@Composable
fun ToolbarButton(
    label: String,
    containerWidthDp: Dp,
    weight: Float,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(1.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = MaterialTheme.shapes.small
            )
            .clickable(enabled = enabled) { onClick() }
            .width(((containerWidthDp * weight) / 100f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) MaterialTheme.colorScheme.background else Color.Black,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
        )
    }
}

