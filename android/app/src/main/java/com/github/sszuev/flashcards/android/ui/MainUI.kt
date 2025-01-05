package com.github.sszuev.flashcards.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel

@Composable
fun MainNavigation(
    onSignOut: () -> Unit = {},
    dictionariesViewModel: DictionaryViewModel,
    cardViewModel: CardViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dictionaries",
    ) {
        composable("dictionaries") {
            DictionariesScreen(
                navController = navController,
                dictionaryViewModel = dictionariesViewModel,
                settingsViewModel = settingsViewModel,
                onSignOut = onSignOut,
                onHomeClick = {
                    dictionariesViewModel.loadDictionaries()
                }
            )
        }
        composable("cards/{dictionaryId}") { backStackEntry ->
            val dictionaryId = backStackEntry.arguments?.getString("dictionaryId") ?: ""
            CardsScreen(
                dictionaryId = dictionaryId,
                viewModel = cardViewModel,
                onSignOut = onSignOut,
                onHomeClick = {
                    navController.navigate("dictionaries")
                },
            )
        }
    }
}