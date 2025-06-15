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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.sszuev.flashcards.android.entities.SettingsEntity
import com.github.sszuev.flashcards.android.models.CardsViewModel
import com.github.sszuev.flashcards.android.models.DictionariesViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.TTSViewModel
import com.github.sszuev.flashcards.android.models.TutorViewModel

private const val tag = "Navigation"

@Composable
fun MainNavigation(
    onSignOut: () -> Unit = {},
    dictionariesViewModel: DictionariesViewModel,
    tutorViewModel: TutorViewModel,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    ttsViewModel: TTSViewModel,
    navController: NavHostController,
) {
    val settingsErrorMessage by settingsViewModel.errorMessage
    val dictionaryErrorMessage by dictionariesViewModel.errorMessage
    val cardsErrorMessage by cardsViewModel.errorMessage

    LaunchedEffect(Unit) {
        dictionariesViewModel.loadDictionariesInit(Locale.current.language)
    }
    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {

            TopBar(
                onSignOut = onSignOut,
                onHomeClick = {
                    cardsViewModel.clearError()
                    navController.navigateToDictionariesPage(
                        dictionariesViewModel,
                        settingsViewModel
                    )
                }
            )

            settingsErrorMessage?.let {
                ErrorMessageBox(it)
            }
            dictionaryErrorMessage?.let {
                ErrorMessageBox(it)
            }
            cardsErrorMessage?.let {
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
                                dictionariesViewModel.loadDictionaries()
                            },
                            dictionariesViewModel = dictionariesViewModel,
                            settingsViewModel = settingsViewModel,
                            cardsViewModel = cardsViewModel,
                            tutorViewModel = tutorViewModel,
                        )
                    }
                    composable("cards/{dictionaryId}") { backStackEntry ->
                        val dictionaryId = backStackEntry.arguments
                            ?.getString("dictionaryId")
                            ?.takeIf { it.isNotBlank() }
                            ?: throw IllegalArgumentException("Can't determine dictionaryId")
                        val dictionary = dictionariesViewModel.dictionaries.value
                            .firstOrNull { it.dictionaryId == dictionaryId }
                        if (dictionary != null) {
                            check(dictionary.dictionaryId == dictionaryId) { "Wrong dictionaryId" }
                            CardsScreen(
                                dictionary = dictionary,
                                cardsViewModel = cardsViewModel,
                                ttsViewModel = ttsViewModel,
                                onHomeClick = {
                                    cardsViewModel.clearError()
                                    navController.navigateToDictionariesPage(
                                        dictionariesViewModel, settingsViewModel
                                    )
                                },
                            )
                        } else {
                            navController.popBackStack("dictionaries", inclusive = false)
                        }
                    }
                    stageChain.forEachIndexed { index, stage ->
                        composable(stage) {
                            when (stage) {
                                "StageShow" -> {
                                    StageShowScreen(
                                        dictionariesViewModel = dictionariesViewModel,
                                        cardsViewModel = cardsViewModel,
                                        tutorViewModel = tutorViewModel,
                                        settingsViewModel = settingsViewModel,
                                        ttsViewModel = ttsViewModel,
                                        onHomeClick = {
                                            navController.navigateToDictionariesPage(
                                                dictionariesViewModel, settingsViewModel
                                            )
                                        },
                                        onNextStage = {
                                            cardsViewModel.clearError()
                                            navController.navigateToNextStage(
                                                stageChain,
                                                index
                                            )
                                        },
                                    )
                                }

                                "StageMosaicDirect" -> StageMosaicScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageMosaicReverse" -> StageMosaicScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageOptionsDirect" -> StageOptionsScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageOptionsReverse" -> StageOptionsScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageWritingDirect" -> StageWritingScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        cardsViewModel.clearError()
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageWritingReverse" -> StageWritingScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageSelfTestDirect" -> StageSelfTestScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = true,
                                )

                                "StageSelfTestReverse" -> StageSelfTestScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    cardsViewModel = cardsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    ttsViewModel = ttsViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
                                        )
                                    },
                                    onNextStage = {
                                        cardsViewModel.clearError()
                                        navController.navigateToNextStage(
                                            stageChain = stageChain,
                                            currentIndex = index
                                        )
                                    },
                                    direction = false,
                                )

                                "StageResult" -> StageResultScreen(
                                    tutorViewModel = tutorViewModel,
                                    dictionariesViewModel = dictionariesViewModel,
                                    onHomeClick = {
                                        navController.navigateToDictionariesPage(
                                            dictionariesViewModel, settingsViewModel
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
    dictionariesViewModel: DictionariesViewModel,
    settingsViewModel: SettingsViewModel,
) {
    if (currentDestination?.route == "dictionaries") {
        Log.i(tag, "Already on 'dictionaries' screen, skipping navigation")
    } else {
        Log.i(tag, "Go to 'dictionaries'")
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
    dictionariesViewModel.loadDictionaries()
}
