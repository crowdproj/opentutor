package com.github.sszuev.flashcards.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.core.net.toUri
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import java.util.Locale

class LoginActivity : ComponentActivity() {

    private val tag = "LoginActivity"

    private lateinit var authService: AuthorizationService

    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "START")

        setContentView(R.layout.activity_login)

        val subtitleTextView = findViewById<TextView>(R.id.subtitle)

        val localizedSubtitle = when (Locale.getDefault().language) {
            "ru" -> "Добро пожаловать в opentutor. Войдите, чтобы сохранить прогресс."
            "fr" -> "Bienvenue sur opentutor. Connectez-vous pour sauvegarder vos progrès."
            "de" -> "Willkommen bei opentutor. Melden Sie sich an, um Ihren Fortschritt zu speichern."
            "es" -> "Bienvenido a opentutor. Inicia sesión para guardar tu progreso."
            "it" -> "Benvenuto su opentutor. Accedi per salvare i tuoi progressi."
            "pt" -> "Bem-vindo ao opentutor. Faça login para salvar seu progresso."
            "zh" -> "欢迎使用 opentutor。请登录以保存您的进度。"
            "ja" -> "opentutor へようこそ。進捗を保存するにはサインインしてください。"
            "ko" -> "opentutor에 오신 것을 환영합니다. 진행 상황을 저장하려면 로그인하세요."
            "tr" -> "opentutor'a hoş geldiniz. İlerlemenizi kaydetmek için giriş yapın."
            "pl" -> "Witamy w opentutor. Zaloguj się, aby zapisać swoje postępy."
            "uk" -> "Ласкаво просимо до opentutor. Увійдіть, щоб зберегти свій прогрес."
            "ar" -> "مرحبًا بك في opentutor. الرجاء تسجيل الدخول لحفظ تقدمك."
            else -> "Welcome to opentutor. Please sign in to save your progress."
        }

        subtitleTextView.text = localizedSubtitle

        authService = AuthorizationService(this)

        authorizationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                Log.i(tag, "ActivityResult::$result")
                val data = result.data!!
                val response = AuthorizationResponse.fromIntent(data)
                val ex = AuthorizationException.fromIntent(data)
                if (response != null) {
                    Log.i(tag, "Authorization response received")
                    exchangeToken(response)
                } else {
                    Log.e(tag, "Authorization failed", ex)
                }
            } else {
                Log.e(tag, "ActivityResult::$result, Extras::${result.data?.extras}")
            }
        }
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            loginButton.isEnabled = false
            loginButton.alpha = 0.5f
            try {
                startLogin()
            } finally {
                loginButton.isEnabled = true
                loginButton.alpha = 1.0f
            }
        }
    }

    private fun startLogin() {
        Log.i(tag, "Login: begin")
        val serverUri = AppConfig.serverUri
        val serviceConfig = AuthorizationServiceConfiguration(
            /* authorizationEndpoint = */ "$serverUri/realms/flashcards-realm/protocol/openid-connect/auth".toUri(),
            /* tokenEndpoint = */
            "$serverUri/realms/flashcards-realm/protocol/openid-connect/token".toUri()
        )

        val authRequest = AuthorizationRequest.Builder(
            /* configuration = */ serviceConfig,
            /* clientId = */ "flashcards-android",
            /* responseType = */ ResponseTypeValues.CODE,
            /* redirectUri = */ "com.github.sszuev.flashcards.android://oauth2redirect".toUri()
        ).setScope("openid profile email").build()

        val customTabsIntent = CustomTabsIntent.Builder().build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest, customTabsIntent)

        authorizationLauncher.launch(authIntent)
        Log.i(tag, "Login: finish")
    }

    private fun exchangeToken(response: AuthorizationResponse) {
        authService.performTokenRequest(
            response.createTokenExchangeRequest()
        ) { tokenResponse, exception ->
            if (tokenResponse != null) {
                val accessToken = tokenResponse.accessToken
                val refreshToken = tokenResponse.refreshToken
                val idToken = tokenResponse.idToken

                Log.d(tag, "Access Token: $accessToken")
                Log.d(tag, "Refresh Token: $refreshToken")
                Log.d(tag, "Id Token: $idToken")

                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit {
                    putString("access_token", accessToken)
                        .putString("refresh_token", refreshToken)
                        .putString("id_token", idToken)
                }

                val intent = Intent(this, MainActivity::class.java)
                Log.i(tag, "go to MainActivity")
                startActivity(intent)
                finish()
            } else {
                Log.e(tag, "Token Exchange Failed", exception)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}
