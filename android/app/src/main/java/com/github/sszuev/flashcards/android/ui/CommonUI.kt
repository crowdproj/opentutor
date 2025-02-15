package com.github.sszuev.flashcards.android.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.utils.getUsernameFromPreferences

@Composable
fun TopBar(
    onSignOut: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val username = getUsernameFromPreferences(LocalContext.current as Activity)
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
        )
    }
}

@Composable
fun HeaderTableCell(
    text: String,
    weight: Int,
    containerWidthDp: Dp,
    onClick: (() -> Unit)? = null,
) {
    TableCell(
        text = text,
        weight = weight,
        containerWidthDp = containerWidthDp,
        onClick = onClick,
        fontSize = 18.sp,
        textColor = Color.Black,
    )
}

@Composable
fun TableCell(
    text: String,
    weight: Int,
    containerWidthDp: Dp,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .width(((containerWidthDp * weight) / 100f))
            .padding(4.dp)
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
        )
    }
}

@Composable
fun TableCellWithPopup(
    shortText: String,
    fullText: String,
    weight: Int,
    containerWidthDp: Dp,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    onShortClick: () -> Unit = {},
) {
    var isPopupVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width((containerWidthDp * weight / 100f))
            .padding(vertical = 4.dp, horizontal = 8.dp)
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
                BorderStroke(2.dp, if (isSelected) borderColor else Color.Transparent),
                shape = MaterialTheme.shapes.small
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.CenterStart
    ) {
        TableCellText(
            text = text,
            fontSize = fontSize,
            lineHeight = lineHeight,
            textColor = textColor
        )
    }
}

@Composable
fun TableCellSelectableWithPopup(
    shortText: String,
    fullText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
    borderColor: Color = Color.Red
) {
    var isPopupVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(
                BorderStroke(2.dp, if (isSelected) borderColor else Color.Transparent),
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
            textColor = textColor
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = 40.sp,
    textColor: Color = Color.DarkGray,
) {
    Text(
        text = text,
        textAlign = TextAlign.Start,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Clip,
        softWrap = true,
        lineHeight = lineHeight,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = fontSize,
            letterSpacing = 0.5.sp,
        )
    )
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
            lineHeight = 46.sp,
        )
    }
}

@Composable
fun SearchableDropdown(
    options: Map<String, String>,
    selectedTag: String?,
    onOptionSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = options[selectedTag] ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            placeholder = { Text(text = "Select...") },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        )

        if (!expanded) {
            return
        }
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
                            .padding(bottom = 8.dp),
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
    viewModel: CardViewModel,
    card: CardEntity,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val cardId = checkNotNull(card.cardId)

    val enabled =  !(viewModel.isAudioProcessing(cardId))

    IconButton(
        onClick = {
            viewModel.loadAndPlayAudio(card)
        },
        enabled = enabled,
        modifier = modifier.padding(start = 8.dp)
    ) {
        if (viewModel.isAudioLoading(cardId)) {
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
    modifier: Modifier = Modifier,
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
