package com.github.sszuev.flashcards.android.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel

private const val tag = "Navigation"

@Composable
fun MainNavigation(
    onSignOut: () -> Unit = {},
    dictionaryViewModel: DictionaryViewModel,
    cardViewModel: CardViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
) {
    val navController = rememberNavController()

    val settingsErrorMessage by settingsViewModel.errorMessage
    val dictionaryErrorMessage by dictionaryViewModel.errorMessage

    LaunchedEffect(Unit) {
        Log.i(tag, "Load Settings")
        settingsViewModel.loadSettings()
    }
    LaunchedEffect(Unit) {
        Log.i(tag, "CURRENT-LOCALE-LANGUAGE::" + Locale.current.language)
        dictionaryViewModel.loadDictionaries(Locale.current.language)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {

            TopBar(
                onSignOut = onSignOut,
                onHomeClick = {
                    navController.navigateToDictionariesPage(dictionaryViewModel, settingsViewModel)
                }
            )

            settingsErrorMessage?.let {
                ErrorMessageBox(it)
            }
            dictionaryErrorMessage?.let {
                ErrorMessageBox(it)
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
                            onHomeClick = {
                                dictionaryViewModel.loadDictionaries()
                            },
                            dictionaryViewModel = dictionaryViewModel,
                            settingsViewModel = settingsViewModel,
                            cardViewModel = cardViewModel
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
                                cardViewModel = cardViewModel,
                                ttsViewModel = ttsViewModel,
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
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                )

                                "StageMosaicDirect" -> StageMosaicScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageMosaicReverse" -> StageMosaicScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageOptionsDirect" -> StageOptionsScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageOptionsReverse" -> StageOptionsScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageWritingDirect" -> StageWritingScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageWritingReverse" -> StageWritingScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageSelfTestDirect" -> StageSelfTestScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageSelfTestReverse" -> StageSelfTestScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        navController.navigateToNextStage(
                                            stageChain,
                                            index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageResult" -> StageResultScreen(
                                    cardViewModel = cardViewModel,
                                    dictionaryViewModel = dictionaryViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionaryViewModel, settingsViewModel
                                        )
                                    },
                                )
                            }
                        }
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

private fun NavController.navigateToDictionariesPage(
    dictionaryViewModel: DictionaryViewModel,
    settingsViewModel: SettingsViewModel,
) {
    Log.i(tag, "Go to 'dictionaries'")

    if (currentDestination?.route == "dictionaries") {
        Log.w(tag, "Already on 'dictionaries' screen, skipping navigation")
    } else {
        try {
            if (currentBackStackEntry != null) {
                navigate("dictionaries") {
                    popUpTo("dictionaries") { inclusive = true }
                }
            } else {
                Log.w(tag, "NavController is not ready yet, skipping navigation")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error navigating to 'dictionaries': ${e.localizedMessage}", e)
        }
    }
    settingsViewModel.loadSettings()
    dictionaryViewModel.loadDictionaries()
}
