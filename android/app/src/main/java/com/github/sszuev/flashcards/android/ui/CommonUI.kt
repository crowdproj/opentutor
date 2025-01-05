package com.github.sszuev.flashcards.android.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.sszuev.flashcards.android.getUsernameFromPreferences

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
                                .filter { it.value.contains(searchQuery, ignoreCase = true) }
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
