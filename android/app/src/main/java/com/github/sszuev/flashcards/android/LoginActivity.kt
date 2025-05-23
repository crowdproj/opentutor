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
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        WindowCompat.setDecorFitsSystemWindows(window, true)
        @Suppress("DEPRECATION")
        window.statusBarColor = "#E3F2FD".toColorInt()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_login)

        val titleTextView = findViewById<TextView>(R.id.title)
        val subtitleTextView = findViewById<TextView>(R.id.subtitle)

        val language = Locale.getDefault().language

        val (localizedTitle, localizedSubtitle) = when (language) {
            "zh" -> "欢迎使用 opentutor" to "请登录以保存您的进度。"
            "hi" -> "opentutor में आपका स्वागत है" to "अपनी प्रगति सहेजने के लिए साइन इन करें।"
            "es" -> "Bienvenido a opentutor" to "Inicia sesión para guardar tu progreso."
            "fr" -> "Bienvenue sur opentutor" to "Connectez-vous pour sauvegarder vos progrès."
            "ar" -> "مرحبًا بك في opentutor" to "الرجاء تسجيل الدخول لحفظ تقدمك."
            "bn" -> "opentutor-এ স্বাগতম" to "আপনার অগ্রগতি সংরক্ষণ করতে সাইন ইন করুন।"
            "pt" -> "Bem-vindo ao opentutor" to "Faça login para salvar seu progresso."
            "ru" -> "Добро пожаловать в opentutor" to "Войдите, чтобы сохранить прогресс."
            "ur" -> "opentutor میں خوش آمدید" to "براہ کرم اپنے پیش رفت کو محفوظ کرنے کے لیے سائن ان کریں۔"
            "id" -> "Selamat datang di opentutor" to "Silakan masuk untuk menyimpan progres Anda."
            "de" -> "Willkommen bei opentutor" to "Melden Sie sich an, um Ihren Fortschritt zu speichern."
            "ja" -> "opentutor へようこそ" to "進捗を保存するにはサインインしてください。"
            "tr" -> "opentutor'a hoş geldiniz" to "İlerlemenizi kaydetmek için giriş yapın."
            "vi" -> "Chào mừng đến với opentutor" to "Vui lòng đăng nhập để lưu tiến trình của bạn."
            "ko" -> "opentutor에 오신 것을 환영합니다" to "진행 상황을 저장하려면 로그인하세요."
            "fa" -> "به opentutor خوش آمدید" to "لطفاً برای ذخیره پیشرفت خود وارد شوید."
            "sw" -> "Karibu kwenye opentutor" to "Tafadhali ingia ili kuhifadhi maendeleo yako."
            "ta" -> "opentutor-க்கு வரவேற்கிறோம்" to "உங்கள் முன்னேற்றத்தை சேமிக்க உள்நுழைக."
            "mr" -> "opentutor मध्ये आपले स्वागत आहे" to "आपली प्रगती जतन करण्यासाठी कृपया साइन इन करा."
            "it" -> "Benvenuto su opentutor" to "Accedi per salvare i tuoi progressi."
            "pl" -> "Witamy w opentutor" to "Zaloguj się, aby zapisać swoje postępy."
            "uk" -> "Ласкаво просимо до opentutor" to "Увійдіть, щоб зберегти свій прогрес."
            else -> "Welcome to opentutor" to "Please sign in to save your progress."
        }

        titleTextView.text = localizedTitle
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
