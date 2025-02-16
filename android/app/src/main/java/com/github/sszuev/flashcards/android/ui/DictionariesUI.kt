package com.github.sszuev.flashcards.android.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.github.sszuev.flashcards.android.entities.DictionaryEntity
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel

private const val tag = "DictionariesUI"
private const val FIRST_COLUMN_WIDTH = 28
private const val SECOND_COLUMN_WIDTH = 19
private const val THIRD_COLUMN_WIDTH = 19
private const val FOURTH_COLUMN_WIDTH = 16
private const val FIFTH_COLUMN_WIDTH = 18

@Composable
fun DictionariesScreen(
    navController: NavHostController,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
    cardViewModel: CardViewModel,
) {
    BackHandler {
        onHomeClick()
    }

    val selectedDictionaryIds = dictionaryViewModel.selectedDictionaryIds
    val isEditPopupOpen = remember { mutableStateOf(false) }
    val isCreatePopupOpen = remember { mutableStateOf(false) }
    val isDeletePopupOpen = remember { mutableStateOf(false) }
    val isSettingsPopupOpen = remember { mutableStateOf(false) }
    val selectedDictionary = dictionaryViewModel.selectedDictionariesList.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
            DictionaryTable(
                viewModel = dictionaryViewModel,
            )
        }
        DictionariesBottomToolbar(
            onCardsClick = {
                if (selectedDictionaryIds.value.size == 1) {
                    val dictionaryId = selectedDictionaryIds.value.first()
                    Log.i(tag, "Go to cards/$dictionaryId")
                    navController.navigate("cards/$dictionaryId")
                }
            },
            onEditClick = {
                if (selectedDictionaryIds.value.size == 1) {
                    isEditPopupOpen.value = true
                }
            },
            onCreateClick = {
                isCreatePopupOpen.value = true
            },
            onDeleteClick = {
                isDeletePopupOpen.value = true
            },
            onSettingsClick = {
                isSettingsPopupOpen.value = true
            },
            onRunClick = {
                if (selectedDictionaryIds.value.isNotEmpty()) {
                    navController.navigate("StageShow")
                }
            },
            selectedDictionaryIds = selectedDictionaryIds.value,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (isEditPopupOpen.value && selectedDictionary != null) {
        EditDictionaryDialog(
            viewModel = dictionaryViewModel,
            dictionary = selectedDictionary,
            onSave = {
                val numberLearnedCards = cardViewModel.numberOfKnownCards(it.numberOfRightAnswers)
                dictionaryViewModel.updateDictionary(it.copy(learnedWords = numberLearnedCards))
            },
            onDismiss = { isEditPopupOpen.value = false }
        )
    }
    if (isCreatePopupOpen.value) {
        AddDictionaryDialog(
            viewModel = dictionaryViewModel,
            onSave = { source, target, name, acceptedNum ->
                val dictionary = DictionaryEntity(
                    dictionaryId = null,
                    name = name,
                    sourceLanguage = source,
                    targetLanguage = target,
                    numberOfRightAnswers = acceptedNum,
                    totalWords = 0,
                    learnedWords = 0,
                )
                dictionaryViewModel.createDictionary(dictionary)
            },
            onDismiss = { isCreatePopupOpen.value = false }
        )
    }
    if (isDeletePopupOpen.value && selectedDictionary != null) {
        DeleteDictionaryDialog(
            dictionaryName = selectedDictionary.name,
            onClose = { isDeletePopupOpen.value = false },
            onConfirm = {
                dictionaryViewModel.deleteDictionary(checkNotNull(selectedDictionary.dictionaryId))
            }
        )
    }
    if (isSettingsPopupOpen.value) {
        val settingsErrorMessage by settingsViewModel.errorMessage
        LaunchedEffect(isSettingsPopupOpen.value) {
            settingsViewModel.loadSettings()
        }
        ErrorMessageBox(settingsErrorMessage)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (settingsViewModel.isLoadSettingsInProgress.value) {
                CircularProgressIndicator()
            } else {
                settingsViewModel.settings.value?.let { initialSettings ->
                    EditSettingsDialog(
                        onDismiss = { isSettingsPopupOpen.value = false },
                        onSave = {
                            settingsViewModel.saveSettings(it)
                        },
                        initialSettings = initialSettings
                    )
                }
            }
        }
    }
}

@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun DictionaryTable(
    viewModel: DictionaryViewModel,
) {
    val dictionaries by viewModel.dictionaries
    val isLoading by viewModel.isDictionariesLoading
    val isLoaded = rememberSaveable { mutableStateOf(false) }
    val errorMessage by viewModel.errorMessage

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (!isLoaded.value) {
            viewModel.loadDictionariesIfNeeded()
            isLoaded.value = true
        }
    }

    LaunchedEffect(dictionaries.size) {
        if (dictionaries.isNotEmpty()) {
            listState.animateScrollToItem(dictionaries.size - 1)
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

                errorMessage != null -> ErrorMessageBox(errorMessage)

                dictionaries.isEmpty() -> {
                    Text(
                        text = "No dictionaries found",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 145.dp)
                    ) {
                        items(dictionaries) { dictionary ->
                            val isSelected =
                                viewModel.selectedDictionaryIds.value.contains(dictionary.dictionaryId)
                            DictionariesTableRow(
                                dictionary = dictionary,
                                containerWidthDp = containerWidthDp,
                                isSelected = isSelected,
                                onSelect = {
                                    viewModel.toggleSelection(
                                        dictionaryId = checkNotNull(dictionary.dictionaryId),
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
            .height(60.dp)
    ) {
        HeaderTableCell(
            text = "Name ${if (currentSortField == "name") if (isAscending) "↑" else "↓" else ""}",
            weight = FIRST_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("name") }
        )
        HeaderTableCell(
            text = "Source ${if (currentSortField == "sourceLanguage") if (isAscending) "↑" else "↓" else ""}",
            weight = SECOND_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("sourceLanguage") }
        )
        HeaderTableCell(
            text = "Target ${if (currentSortField == "targetLanguage") if (isAscending) "↑" else "↓" else ""}",
            weight = THIRD_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("targetLanguage") }
        )
        HeaderTableCell(
            text = "Words ${if (currentSortField == "totalWords") if (isAscending) "↑" else "↓" else ""}",
            weight = FOURTH_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("totalWords") }
        )
        HeaderTableCell(
            text = "Learned ${if (currentSortField == "learnedWords") if (isAscending) "↑" else "↓" else ""}",
            weight = FIFTH_COLUMN_WIDTH,
            containerWidthDp = containerWidthDp,
            onClick = { onSort("learnedWords") }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DictionariesTableRow(
    dictionary: DictionaryEntity,
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
    TableCell(text = first, weight = FIRST_COLUMN_WIDTH, containerWidthDp = containerWidthDp)
    TableCell(text = second, weight = SECOND_COLUMN_WIDTH, containerWidthDp = containerWidthDp)
    TableCell(text = third, weight = THIRD_COLUMN_WIDTH, containerWidthDp = containerWidthDp)
    TableCell(text = fourth, weight = FOURTH_COLUMN_WIDTH, containerWidthDp = containerWidthDp)
    TableCell(text = fifth, weight = FIFTH_COLUMN_WIDTH, containerWidthDp = containerWidthDp)
}

@Composable
fun DictionariesBottomToolbar(
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .padding(8.dp)
            .onSizeChanged { size -> containerWidthPx = size.width },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                label = "RUN",
                containerWidthDp = containerWidthDp,
                weight = 21.43f,
                onClick = onRunClick,
                enabled = selectedDictionaryIds.isNotEmpty(),
            )
            ToolbarButton(
                label = "CARDS",
                containerWidthDp = containerWidthDp,
                weight = 35.71f,
                onClick = onCardsClick,
                enabled = selectedDictionaryIds.size == 1,
            )
            ToolbarButton(
                label = "CREATE",
                containerWidthDp = containerWidthDp,
                weight = 42.86f,
                onClick = onCreateClick
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                label = "EDIT",
                containerWidthDp = containerWidthDp,
                weight = 23.53f,
                onClick = onEditClick,
                enabled = selectedDictionaryIds.size == 1,
            )
            ToolbarButton(
                label = "DELETE",
                containerWidthDp = containerWidthDp,
                weight = 35.29f,
                onClick = onDeleteClick,
                enabled = selectedDictionaryIds.size == 1,
            )
            ToolbarButton(
                label = "SETTINGS",
                containerWidthDp = containerWidthDp,
                weight = 41.18f,
                onClick = onSettingsClick,
                enabled = true,
            )
        }
    }
}

@Composable
fun EditDictionaryDialog(
    dictionary: DictionaryEntity,
    viewModel: DictionaryViewModel,
    onSave: (DictionaryEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var dictionaryName by remember { mutableStateOf(dictionary.name) }
    var numberOfRightAnswers by remember { mutableStateOf(dictionary.numberOfRightAnswers.toString()) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.ime),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.imePadding(),
            ) {
                item {
                    Text(
                        text = "${viewModel.languages[dictionary.sourceLanguage]} -> ${viewModel.languages[dictionary.targetLanguage]}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }

                item {
                    Text(
                        text = "NAME:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextField(
                        value = dictionaryName,
                        onValueChange = { dictionaryName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "ACCEPTED ANSWERS:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextField(
                        value = numberOfRightAnswers,
                        onValueChange = {
                            numberOfRightAnswers = it.filter { char -> char.isDigit() }
                        },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                        ) {
                            Text(text = "CLOSE", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                val updatedDictionary = dictionary.copy(
                                    name = dictionaryName,
                                    numberOfRightAnswers = numberOfRightAnswers.toIntOrNull() ?: 0
                                )
                                onSave(updatedDictionary)
                                onDismiss()
                            }
                        ) {
                            Text(text = "SAVE", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddDictionaryDialog(
    viewModel: DictionaryViewModel,
    onSave: (String, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var dictionaryName by remember { mutableStateOf("") }
    var numberOfRightAnswers by remember { mutableStateOf("15") }
    var selectedSourceLanguageTag by remember { mutableStateOf<String?>(null) }
    var selectedTargetLanguageTag by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.ime),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.imePadding(),
            ) {
                // Source Language
                item {
                    Text(text = "SOURCE:", style = MaterialTheme.typography.bodyLarge)
                    SearchableDropdown(
                        options = viewModel.languages,
                        selectedTag = selectedSourceLanguageTag,
                        onOptionSelect = { selectedSourceLanguageTag = it }
                    )
                }

                // Target Language
                item {
                    Text(text = "TARGET:", style = MaterialTheme.typography.bodyLarge)
                    SearchableDropdown(
                        options = viewModel.languages,
                        selectedTag = selectedTargetLanguageTag,
                        onOptionSelect = { selectedTargetLanguageTag = it }
                    )
                }

                // Dictionary Name
                item {
                    Text(text = "Dictionary Name:", style = MaterialTheme.typography.bodyLarge)
                    TextField(
                        value = dictionaryName,
                        onValueChange = { dictionaryName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "ACCEPTED ANSWERS:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextField(
                        value = numberOfRightAnswers,
                        onValueChange = {
                            numberOfRightAnswers = it.filter { char -> char.isDigit() }
                        },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = onDismiss) {
                            Text(text = "CLOSE")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                if (selectedSourceLanguageTag != null && selectedTargetLanguageTag != null && dictionaryName.isNotBlank()) {
                                    onSave(
                                        checkNotNull(selectedSourceLanguageTag),
                                        checkNotNull(selectedTargetLanguageTag),
                                        dictionaryName,
                                        numberOfRightAnswers.toInt(),
                                    )
                                    onDismiss()
                                }
                            }
                        ) {
                            Text(text = "SAVE")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteDictionaryDialog(
    dictionaryName: String,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("DELETE:") },
        text = { Text(dictionaryName) },
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
fun EditSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (SettingsEntity) -> Unit,
    initialSettings: SettingsEntity,
) {
    val showWordsCount =
        remember { mutableStateOf(initialSettings.stageShowNumberOfWords.toString()) }
    val optionsVariantsCount =
        remember { mutableStateOf(initialSettings.stageOptionsNumberOfVariants.toString()) }
    val wordsPerStageCount =
        remember { mutableStateOf(initialSettings.numberOfWordsPerStage.toString()) }

    val checkboxStates = remember {
        mutableStateMapOf(
            "mosaicSourceTarget" to initialSettings.stageMosaicSourceLangToTargetLang,
            "optionsSourceTarget" to initialSettings.stageOptionsSourceLangToTargetLang,
            "writingSourceTarget" to initialSettings.stageWritingSourceLangToTargetLang,
            "selfTestSourceTarget" to initialSettings.stageSelfTestSourceLangToTargetLang,
            "mosaicTargetSource" to initialSettings.stageMosaicTargetLangToSourceLang,
            "optionsTargetSource" to initialSettings.stageOptionsTargetLangToSourceLang,
            "writingTargetSource" to initialSettings.stageWritingTargetLangToSourceLang,
            "selfTestTargetSource" to initialSettings.stageSelfTestTargetLangToSourceLang,
        )
    }

    val checkboxLabels = mapOf(
        "mosaicSourceTarget" to "Mosaic [source -> target]",
        "optionsSourceTarget" to "Options [source -> target]",
        "writingSourceTarget" to "Writing [source -> target]",
        "selfTestSourceTarget" to "SelfTest [source -> target]",
        "mosaicTargetSource" to "Mosaic [target -> source]",
        "optionsTargetSource" to "Options [target -> source]",
        "writingTargetSource" to "Writing [target -> source]",
        "selfTestTargetSource" to "SelfTest [target -> source]",
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp)
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .imePadding(),
            ) {
                item {
                    Text(
                        text = "Edit Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = showWordsCount.value,
                        onValueChange = {
                            showWordsCount.value = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Number of words in stage \"Show\" [min=2, max=20]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = optionsVariantsCount.value,
                        onValueChange = {
                            optionsVariantsCount.value = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Number of variants in stage \"Options\" [min=2, max=15]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = wordsPerStageCount.value,
                        onValueChange = {
                            wordsPerStageCount.value = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Number of words per stage [min=2, max=20]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                items(checkboxLabels.keys.toList()) { id ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            checkboxLabels[id] ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = checkboxStates[id] ?: false,
                            onCheckedChange = { checked ->
                                checkboxStates[id] = checked
                            }
                        )
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onDismiss) {
                            Text("CANCEL")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val updatedSettings = initialSettings.copy(
                                stageShowNumberOfWords = showWordsCount.value.toIntOrNull()
                                    ?: initialSettings.stageShowNumberOfWords,
                                stageOptionsNumberOfVariants = optionsVariantsCount.value.toIntOrNull()
                                    ?: initialSettings.stageOptionsNumberOfVariants,
                                numberOfWordsPerStage = wordsPerStageCount.value.toIntOrNull()
                                    ?: initialSettings.numberOfWordsPerStage,
                                stageMosaicSourceLangToTargetLang = checkboxStates["mosaicSourceTarget"]
                                    ?: false,
                                stageOptionsSourceLangToTargetLang = checkboxStates["optionsSourceTarget"]
                                    ?: false,
                                stageWritingSourceLangToTargetLang = checkboxStates["writingSourceTarget"]
                                    ?: false,
                                stageSelfTestSourceLangToTargetLang = checkboxStates["selfTestSourceTarget"]
                                    ?: false,
                                stageMosaicTargetLangToSourceLang = checkboxStates["mosaicTargetSource"]
                                    ?: false,
                                stageOptionsTargetLangToSourceLang = checkboxStates["optionsTargetSource"]
                                    ?: false,
                                stageWritingTargetLangToSourceLang = checkboxStates["writingTargetSource"]
                                    ?: false,
                                stageSelfTestTargetLangToSourceLang = checkboxStates["selfTestTargetSource"]
                                    ?: false,
                            )
                            onSave(updatedSettings)
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
