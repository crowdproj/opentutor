package com.github.sszuev.flashcards.android.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.sszuev.flashcards.android.getUsernameFromPreferences

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