package com.github.sszuev.flashcards.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.sszuev.flashcards.android.models.CardsViewModel

@Composable
fun CardsScreen(
    viewModel: CardsViewModel,
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
            }
        }
    }
}