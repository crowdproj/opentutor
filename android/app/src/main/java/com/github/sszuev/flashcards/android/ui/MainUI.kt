package com.github.sszuev.flashcards.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel

@Composable
fun MainNavigation(
    onSignOut: () -> Unit = {},
    dictionariesViewModel: DictionaryViewModel,
    cardsViewModel: CardsViewModel,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dictionaries",
    ) {
        composable("dictionaries") {
            DictionariesScreen(
                navController = navController,
                viewModel = dictionariesViewModel,
                onSignOut = onSignOut,
            )
        }
        composable("cards") {
            CardsScreen(
                viewModel = cardsViewModel,
                onSignOut = onSignOut,
                onHomeClick = { navController.navigate("dictionaries") },
            )
        }
    }
}