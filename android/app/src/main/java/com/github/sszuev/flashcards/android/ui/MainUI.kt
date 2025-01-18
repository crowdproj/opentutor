package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel

private const val tag = "Navigation"

@Composable
fun MainNavigation(
    onSignOut: () -> Unit = {},
    dictionaryViewModel: DictionaryViewModel,
    cardViewModel: CardViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }

    val settings = settingsViewModel.settings.value
    if (settings == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val stageChain = buildStageChain(settings)

        NavHost(
            navController = navController,
            startDestination = "dictionaries",
        ) {
            composable("dictionaries") {
                DictionariesScreen(
                    navController = navController,
                    dictionaryViewModel = dictionaryViewModel,
                    settingsViewModel = settingsViewModel,
                    cardViewModel = cardViewModel,
                    onSignOut = onSignOut,
                    onHomeClick = {
                        dictionaryViewModel.loadDictionaries()
                    }
                )
            }
            composable("cards/{dictionaryId}") { backStackEntry ->
                val dictionaryId = backStackEntry.arguments
                    ?.getString("dictionaryId")
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Can't determine dictionaryId")
                val dictionary = dictionaryViewModel.selectedDictionariesList.singleOrNull()
                if (dictionary != null) {
                    check(dictionary.dictionaryId == dictionaryId) { "Wrong dictionaryId" }
                    CardsScreen(
                        dictionary = dictionary,
                        viewModel = cardViewModel,
                        onSignOut = onSignOut,
                        onHomeClick = {
                            navController.navigateToDictionariesPage()
                        },
                    )
                } else {
                    navController.popBackStack("dictionaries", inclusive = false)
                }
            }
            stageChain.forEachIndexed { index, stage ->
                composable(stage) {
                    when (stage) {
                        "StageShow" -> StageShowScreen(
                            dictionaryViewModel = dictionaryViewModel,
                            cardViewModel = cardViewModel,
                            settingsViewModel = settingsViewModel,
                            onSignOut = onSignOut,
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                        )

                        "StageMosaicDirect" -> StageMosaicScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onSignOut = onSignOut,
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            direction = true,
                        )

                        "StageMosaicReverse" -> StageMosaicScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onSignOut = onSignOut,
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            direction = false,
                        )

                        "StageOptionsDirect" -> StageOptionsScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onSignOut = onSignOut,
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            direction = true,
                        )

                        "StageOptionsReverse" -> StageOptionsScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onSignOut = onSignOut,
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            direction = false,
                        )

                        "StageWritingDirect" -> StageWritingScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            direction = true,
                            onSignOut = onSignOut,
                        )

                        "StageWritingReverse" -> StageWritingScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            direction = false,
                            onSignOut = onSignOut,
                        )

                        "StageSelfTestDirect" -> StageSelfTestScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            direction = true,
                            onSignOut = onSignOut,
                        )

                        "StageSelfTestReverse" -> StageSelfTestScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            onNextStage = { navController.navigateToNextStage(stageChain, index) },
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            direction = false,
                            onSignOut = onSignOut,
                        )

                        "StageResult" -> StageResultScreen(
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionaryViewModel,
                            onHomeClick = { navController.navigateToDictionariesPage() },
                            onSignOut = onSignOut,
                        )
                    }
                }
            }
        }
    }
}

fun buildStageChain(settings: SettingsEntity): List<String> {
    val stages = mutableListOf("StageShow")

    if (settings.stageMosaicSourceLangToTargetLang) stages.add("StageMosaicDirect")
    if (settings.stageMosaicTargetLangToSourceLang) stages.add("StageMosaicReverse")
    if (settings.stageOptionsSourceLangToTargetLang) stages.add("StageOptionsDirect")
    if (settings.stageOptionsTargetLangToSourceLang) stages.add("StageOptionsReverse")
    if (settings.stageWritingSourceLangToTargetLang) stages.add("StageWritingDirect")
    if (settings.stageWritingTargetLangToSourceLang) stages.add("StageWritingReverse")
    if (settings.stageSelfTestSourceLangToTargetLang) stages.add("StageSelfTestDirect")
    if (settings.stageSelfTestTargetLangToSourceLang) stages.add("StageSelfTestReverse")

    stages.add("StageResult")

    return stages
}

private fun NavController.navigateToNextStage(
    stageChain: List<String>,
    currentIndex: Int,
) {
    if (currentIndex < stageChain.size - 1) {
        val stage = stageChain[currentIndex + 1]
        if (currentBackStackEntry?.destination?.route != stage) {
            Log.i(tag, "Go to '$stage'")
            navigate(stage) {
                launchSingleTop = true
                popUpTo(stageChain[currentIndex]) { inclusive = true }
            }
        }
    }
}

private fun NavController.navigateToDictionariesPage() {
    Log.i(tag, "Go to 'dictionaries'")
    navigate("dictionaries")
}