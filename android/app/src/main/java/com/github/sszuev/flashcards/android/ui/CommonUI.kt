package com.github.sszuev.flashcards.android.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import com.github.sszuev.flashcards.android.entities.CardEntity
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.utils.username

val SELECTED_ROW_COLOR = Color(0xFF90CAF9)

private const val tag = "CommonUI"

@Composable
fun TopBar(
    onSignOut: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val username = LocalContext.current.username()
    val style = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Absolute.Right,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "[ $username ]",
            style = style,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = "HOME",
            color = Color.Blue,
            style = style,
            modifier = Modifier
                .clickable { onHomeClick() }
                .padding(end = 16.dp)
        )
        Text(
            text = "SIGN OUT",
            color = Color.Blue,
            style = style,
            modifier = Modifier
                .clickable { onSignOut() }
                .focusable(true)
                .semantics {
                    contentDescription = "SIGN_OUT"
                    role = androidx.compose.ui.semantics.Role.Button
                }
        )
    }
}

@Composable
fun HeaderTableCell(
    text: String,
    weight: Int,
    containerWidthDp: Dp,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.W900,
    textAlign: TextAlign = TextAlign.Start,
    onClick: (() -> Unit)? = null,
) {
    TableCell(
        text = text,
        weight = weight,
        containerWidthDp = containerWidthDp,
        onClick = onClick,
        fontSize = fontSize,
        textColor = Color.Black,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}

@Composable
fun TableCell(
    text: String,
    weight: Int,
    textAlign: TextAlign = TextAlign.Start,
    containerWidthDp: Dp,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    softWrap: Boolean = true,
    onClick: (() -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Bold,
    padding: Dp = 1.dp,
) {
    Box(
        modifier = Modifier
            .width(((containerWidthDp * weight) / 100f))
            .padding(padding)
            .let {
                if (onClick != null) {
                    it.clickable { onClick() }
                } else {
                    it
                }
            },
        contentAlignment = Alignment.TopStart
    ) {
        TableCellText(
            text = text,
            lineHeight = lineHeight,
            textColor = textColor,
            fontSize = fontSize,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            fontWeight = fontWeight,
            textAlign = textAlign,
            innerPadding = 0.dp,
        )
    }
}

@Composable
fun TableCellWithPopup(
    shortText: String,
    fullText: String,
    weight: Int,
    containerWidthDp: Dp,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    onShortClick: () -> Unit = {},
    padding: Dp = 1.dp,
) {
    var isPopupVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width((containerWidthDp * weight / 100f))
            .padding(padding)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onShortClick()
                    },
                    onLongPress = {
                        isPopupVisible = true
                    }
                )
            },
        contentAlignment = Alignment.TopStart
    ) {
        TableCellText(
            text = shortText,
            lineHeight = lineHeight,
            textColor = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            innerPadding = 0.dp,
        )
        if (isPopupVisible) {
            TablePopup(
                text = fullText,
                fontSize = fontSize,
                lineHeight = lineHeight,
                onClose = { isPopupVisible = false },
            )
        }
    }
}

@Composable
fun TableCellSelectable(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    borderColor: Color = Color.Red,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(
                border = BorderStroke(2.dp, if (isSelected) borderColor else Color.LightGray),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.CenterStart
    ) {
        TableCellText(
            text = text,
            fontSize = fontSize,
            lineHeight = lineHeight,
            textColor = textColor,
            textAlign = textAlign,
            innerPadding = 8.dp,
        )
    }
}

@Composable
fun TableCellSelectableWithPopup(
    shortText: String,
    fullText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    borderColor: Color = Color.Red
) {
    var isPopupVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(
                BorderStroke(2.dp, if (isSelected) borderColor else Color.LightGray),
                shape = MaterialTheme.shapes.small
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onLongPress = { isPopupVisible = true }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        TableCellText(
            text = shortText,
            fontSize = fontSize,
            lineHeight = lineHeight,
            textColor = textColor,
            textAlign = textAlign,
            innerPadding = 8.dp,
        )

        if (isPopupVisible) {
            TablePopup(
                text = fullText,
                onClose = { isPopupVisible = false },
                fontSize = fontSize,
                lineHeight = lineHeight
            )
        }
    }
}

@Composable
fun TableCellWithContent(
    weight: Int,
    containerWidthDp: Dp,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .width((containerWidthDp * weight / 100f))
            .padding(1.dp),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

@Composable
fun TablePopup(
    text: String,
    onClose: () -> Unit,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp
) {
    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onClose
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .padding(8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close popup"
                        )
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = fontSize,
                        lineHeight = lineHeight
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TableCellText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    innerPadding: Dp = 2.dp,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    val alignment = when (textAlign) {
        TextAlign.Right -> Alignment.CenterEnd
        TextAlign.Center -> Alignment.Center
        else -> Alignment.CenterStart
    }
    Box(
        modifier = modifier
            .padding(innerPadding)
            .fillMaxWidth(), contentAlignment = alignment
    ) {
        Text(
            text = text,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            softWrap = softWrap,
            lineHeight = lineHeight,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = fontWeight,
                color = textColor,
                fontSize = fontSize,
                letterSpacing = 0.5.sp,
            )
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
    if (containerWidthDp <= 0.dp) {
        return
    }
    val buttonWidth = (containerWidthDp * weight / 100f).coerceAtLeast(64.dp)
    Box(
        modifier = Modifier
            .padding(1.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = MaterialTheme.shapes.small
            )
            .clickable(enabled = enabled) { onClick() }
            .width(buttonWidth),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) MaterialTheme.colorScheme.background else Color.Black,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 46.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun SearchableDropdown(
    options: Map<String, String>,
    selectedTag: String?,
    onOptionSelect: (String) -> Unit,
    id: Int,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            value = options[selectedTag] ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth(),
            readOnly = true,
            placeholder = { Text(text = "Select...") },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = !expanded }
                .semantics {
                    contentDescription = "SelectField$id"
                },
        )
    }

    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Column {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .semantics {
                                contentDescription = "SearchField$id"
                            },
                        placeholder = { Text("Search...") }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(
                            options.entries
                                .filter { it.value.startsWith(searchQuery, ignoreCase = true) }
                                .toList()
                        ) { entry ->
                            Text(
                                text = entry.value,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOptionSelect(entry.key)
                                        expanded = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { expanded = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CLOSE")
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayerIcon(
    ttsViewModel: TTSViewModel,
    card: CardEntity,
    size: Dp = 24.dp,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val cardId = card.audioId.takeIf { it.isNotBlank() }
    if (cardId == null) {
        Log.w(tag, "No audionId for $cardId, ${card.word}")
        return
    }

    val enabled = !(ttsViewModel.isAudioProcessing(card.audioId))

    IconButton(
        onClick = {
            ttsViewModel.loadAndPlayAudio(card.audioId)
        },
        enabled = enabled,
        modifier = modifier.padding(start = 8.dp)
    ) {
        if (ttsViewModel.isAudioLoading(card.audioId)) {
            CircularProgressIndicator(modifier = Modifier.size(size))
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Play word audio",
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
fun TextWithPopup(
    shortText: String,
    fullText: String,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    popupFontSize: TextUnit = 20.sp,
    popupLineHeight: TextUnit = 40.sp,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    textColor: Color = Color.Black
) {
    var isPopupVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { isPopupVisible = true }
                )
            }
    ) {
        Text(
            text = shortText,
            style = style.copy(fontSize = fontSize, lineHeight = lineHeight, color = textColor)
        )

        if (isPopupVisible) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { isPopupVisible = false }
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { isPopupVisible = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close popup"
                                )
                            }
                        }

                        Text(
                            text = fullText,
                            style = style.copy(
                                fontSize = popupFontSize,
                                lineHeight = popupLineHeight,
                                color = textColor
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessageBox(errorMessage: String?) {
    if (errorMessage.isNullOrEmpty()) {
        return
    }
    Log.e(tag, errorMessage)
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFCDD2))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun FadeLazyColumn(
    modifier: Modifier = Modifier,
    topFadeHeight: Float = 100f,
    bottomFadeHeight: Float = 100f,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    val showTopFade by remember {
        derivedStateOf {
            val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            firstVisible > 0
        }
    }

    val showBottomFade by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleCount = layoutInfo.visibleItemsInfo.size
            val totalCount = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalCount > visibleCount && lastVisible < totalCount - 1
        }
    }

    Box(
        modifier = modifier
            .drawWithContent {
                drawContent()
                if (showTopFade) {
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(Color.White, Color.Transparent),
                            startY = 0f,
                            endY = topFadeHeight
                        )
                    )
                }
                if (showBottomFade) {
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White),
                            startY = size.height - bottomFadeHeight,
                            endY = size.height
                        )
                    )
                }
            }
    ) {
        LazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
fun StageHeader(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .padding(12.dp)
        )
    }
}