package com.github.sszuev.flashcards.android.ui

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

@Composable
fun MainNavigation(
    onSignOut: () -> Unit = {},
    dictionariesViewModel: DictionaryViewModel,
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
                    dictionaryViewModel = dictionariesViewModel,
                    settingsViewModel = settingsViewModel,
                    cardViewModel = cardViewModel,
                    onSignOut = onSignOut,
                    onHomeClick = {
                        dictionariesViewModel.loadDictionaries()
                    }
                )
            }
            composable("cards/{dictionaryId}") { backStackEntry ->
                val dictionaryId = backStackEntry.arguments
                    ?.getString("dictionaryId")
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Can't determine dictionaryId")
                val dictionary = dictionariesViewModel.selectedDictionariesList.singleOrNull()
                if (dictionary != null) {
                    check(dictionary.dictionaryId == dictionaryId) { "Wrong dictionaryId" }
                    CardsScreen(
                        dictionary = dictionary,
                        viewModel = cardViewModel,
                        onSignOut = onSignOut,
                        onHomeClick = {
                            navController.navigate("dictionaries")
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
                            cardViewModel = cardViewModel,
                            dictionaryViewModel = dictionariesViewModel,
                            settingsViewModel = settingsViewModel,
                            onNextStage = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            onResultStage = { navController.navigate("StageResult") },
                            onSignOut = onSignOut,
                        )

                        "StageMosaicDirect" -> StageMosaicScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = true,
                            onSignOut = onSignOut,
                        )

                        "StageMosaicReverse" -> StageMosaicScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = false,
                            onSignOut = onSignOut,
                        )

                        "StageOptionsDirect" -> StageOptionsScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = true,
                            onSignOut = onSignOut,
                        )

                        "StageOptionsReverse" -> StageOptionsScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = false,
                            onSignOut = onSignOut,
                        )

                        "StageWritingDirect" -> StageWritingScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = true,
                            onSignOut = onSignOut,
                        )

                        "StageWritingReverse" -> StageWritingScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = false,
                            onSignOut = onSignOut,
                        )

                        "StageSelfTestDirect" -> StageSelfTestScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = true,
                            onSignOut = onSignOut,
                        )

                        "StageSelfTestReverse" -> StageSelfTestScreen(
                            onNext = { navigateToNextStage(navController, stageChain, index) },
                            onHomeClick = { navController.navigate("dictionaries") },
                            direction = false,
                            onSignOut = onSignOut,
                        )

                        "StageResult" -> StageResultScreen(
                            cardViewModel = cardViewModel,
                            dictionariesViewModel = dictionariesViewModel,
                            onHomeClick = { navController.navigate("dictionaries") },
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

private fun navigateToNextStage(
    navController: NavController,
    stageChain: List<String>,
    currentIndex: Int
) {
    if (currentIndex < stageChain.size - 1) {
        navController.navigate(stageChain[currentIndex + 1]) {
            popUpTo(stageChain[currentIndex]) { inclusive = true }
        }
    }
}