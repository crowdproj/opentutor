package com.github.sszuev.flashcards.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class LoginActivity : ComponentActivity() {

    private val tag = "LoginActivity"

    private lateinit var authService: AuthorizationService

    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "START")

        setContentView(R.layout.activity_login)

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

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startLogin()
        }
    }

    private fun startLogin() {
        Log.i(tag, "Login: begin")
        val serverUri = AppConfig.serverUri
        val serviceConfig = AuthorizationServiceConfiguration(
            /* authorizationEndpoint = */ Uri.parse("$serverUri/realms/flashcards-realm/protocol/openid-connect/auth"),
            /* tokenEndpoint = */ Uri.parse("$serverUri/realms/flashcards-realm/protocol/openid-connect/token")
        )

        val authRequest = AuthorizationRequest.Builder(
            /* configuration = */ serviceConfig,
            /* clientId = */ "flashcards-android",
            /* responseType = */ ResponseTypeValues.CODE,
            /* redirectUri = */ Uri.parse("com.github.sszuev.flashcards.android://oauth2redirect")
        ).setScope("openid profile email").build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)

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
                prefs.edit()
                    .putString("access_token", accessToken)
                    .putString("refresh_token", refreshToken)
                    .putString("id_token", idToken)
                    .apply()

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
