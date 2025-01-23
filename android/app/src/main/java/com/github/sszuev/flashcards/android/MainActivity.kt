package com.github.sszuev.flashcards.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.github.sszuev.flashcards.android.models.CardViewModel
import com.github.sszuev.flashcards.android.models.CardsViewModelFactory
import com.github.sszuev.flashcards.android.models.DictionariesViewModelFactory
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModel
import com.github.sszuev.flashcards.android.models.SettingsViewModelFactory
import com.github.sszuev.flashcards.android.repositories.CardsRepository
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository
import com.github.sszuev.flashcards.android.repositories.SettingsRepository
import com.github.sszuev.flashcards.android.repositories.TTSRepository
import com.github.sszuev.flashcards.android.repositories.TranslationRepository
import com.github.sszuev.flashcards.android.ui.MainNavigation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"

    private val dictionaryViewModel: DictionaryViewModel by viewModels {
        DictionariesViewModelFactory(
            repository = DictionaryRepository(AppConfig.serverUri),
            signOut = { onSignOut() },
        )
    }
    private val cardViewModel: CardViewModel by viewModels {
        CardsViewModelFactory(
            context = application,
            cardsRepository = CardsRepository(AppConfig.serverUri),
            ttsRepository = TTSRepository(AppConfig.serverUri),
            translationRepository = TranslationRepository(AppConfig.serverUri),
            signOut = { onSignOut() }
        )
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            repository = SettingsRepository(AppConfig.serverUri),
            signOut = { onSignOut() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_FlashcardsAndroid)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null)

        if (accessToken.isNullOrEmpty()) {
            Log.w(tag, "NO TOKEN")
            navigateToLogin()
            return
        }

        setContent {
            MaterialTheme {
                MainNavigation(
                    onSignOut = { onSignOut() },
                    dictionaryViewModel = dictionaryViewModel,
                    cardViewModel = cardViewModel,
                    settingsViewModel = settingsViewModel,
                )
            }
        }
    }

    private fun onSignOut() {
        Log.i(tag, "SIGN OUT")
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)

        val idToken = prefs.getString("id_token", null)

        if (idToken != null) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    performLogoutRequest(idToken)
                }
                prefs.edit()
                    .remove("access_token")
                    .remove("refresh_token")
                    .remove("id_token")
                    .apply()
                navigateToLogin()
            }
        } else {
            Log.e(tag, "Unknown id token")
            prefs.edit()
                .remove("access_token")
                .remove("refresh_token")
                .remove("id_token")
                .apply()
            navigateToLogin()
        }
    }

    private suspend fun performLogoutRequest(idToken: String) {
        val serverUri = AppConfig.serverUri
        val logoutUrl = "$serverUri/realms/flashcards-realm/protocol/openid-connect/logout"
        val params = Parameters.build {
            append("client_id", "flashcards-android")
            append("id_token_hint", idToken)
            append("post_logout_redirect_uri", serverUri)
        }

        try {
            val response: HttpResponse = httpClient.get(logoutUrl) {
                url {
                    parameters.appendAll(params)
                }
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NoContent) {
                Log.i(tag, "Logout successful: ${response.status}")
            } else {
                Log.e(tag, "Logout failed with response code: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error performing logout", e)
        }
    }

    private fun navigateToLogin() {
        Log.i(tag, "Got to LoginActivity")
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        Log.i(tag, "MainActivity::finish")
        finish()
    }

}
