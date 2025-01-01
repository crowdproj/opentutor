package com.github.sszuev.flashcards.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.github.sszuev.flashcards.android.models.DictionaryViewModel
import com.github.sszuev.flashcards.android.models.ViewModelFactory
import com.github.sszuev.flashcards.android.repositories.DictionaryRepository
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"

    private val viewModel: DictionaryViewModel by viewModels {
        ViewModelFactory(DictionaryRepository(AppConfig.serverUri))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_FlashcardsAndroid)
        Log.d(tag, "onCreate::start")

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null)

        if (accessToken.isNullOrEmpty()) {
            Log.w(tag, "NO TOKEN")
            navigateToLogin()
            return
        }
        Log.d(tag, "access token = $accessToken")

        setContent {
            MaterialTheme {
                MainScreen(
                    onSignOut = { onSignOut() },
                    viewModel = viewModel,
                )
            }
        }
        Log.d(tag, "onCreate::done")
    }

    private fun onSignOut() {
        Log.i(tag, "LOG OUT")
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

        val client = clientProducer()
        try {
            val response: HttpResponse = client.get(logoutUrl) {
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
        } finally {
            client.close()
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

//@Preview
@Composable
fun MainScreen(
    onSignOut: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    viewModel: DictionaryViewModel,
) {
    Column {
        TopBar(onSignOut = onSignOut, onHomeClick = onHomeClick)
        DictionaryTable(
            viewModel = viewModel,
        )
    }
}

@Composable
fun TopBar(
    onSignOut: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val username = getUsernameFromPreferences(LocalContext.current as Activity)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Absolute.Right,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[ $username ]",
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = "home",
            color = Color.Blue,
            modifier = Modifier
                .clickable { onHomeClick() }
                .padding(end = 16.dp)
        )
        Text(
            text = "sign out",
            color = Color.Blue,
            modifier = Modifier
                .clickable { onSignOut() }
        )
    }
}

@Composable
fun DictionaryTable(
   viewModel: DictionaryViewModel,
) {
    val dictionaries by viewModel.dictionaries
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerWidthPx.toDp() }

    LaunchedEffect(Unit) {
        viewModel.loadDictionaries()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size -> containerWidthPx = size.width }
    ) {
        if (containerWidthDp > 0.dp) {
            Column {
                TableHeader(containerWidthDp)

                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    errorMessage != null -> {
                        Text(
                            text = checkNotNull(errorMessage),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    dictionaries.isEmpty() -> {
                        Text(
                            text = "No dictionaries found",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> {
                        dictionaries.forEach { row ->
                            TableRow(row, containerWidthDp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeader(containerWidthDp: Dp) {
    Row(
        modifier = Modifier
            //.fillMaxWidth()
            //.requiredSize(10.dp)
            .background(Color.LightGray)
            //.padding(8.dp)
            .border(1.dp, Color.Black)
            .height(100.dp)
    ) {
        TableRow(
            first = "Dictionary name",
            second = "Source language",
            third = "Target language",
            fourth = "Total number of words",
            fifth = "Number of learned words",
            containerWidthDp = containerWidthDp,
        )
    }
}

@Composable
fun TableRow(row: Dictionary, containerWidthDp: Dp) {
    Row(
        modifier = Modifier
            //.requiredSize(10.dp)
            //.fillMaxWidth()
            //.width(0.2.dp)
            //.padding(8.dp)
            .border(1.dp, Color.Black)
        //.height(IntrinsicSize.Min)
    ) {

        TableRow(
            first = row.name,
            second = row.sourceLanguage,
            third = row.targetLanguage,
            fourth = row.totalWords.toString(),
            fifth = row.learnedWords.toString(),
            containerWidthDp = containerWidthDp,
        )
    }
}

@Composable
fun TableRow(
    first: String,
    second: String,
    third: String,
    fourth: String,
    fifth: String,
    containerWidthDp: Dp
) {
    TableCell(text = first, weight = 28, containerWidthDp = containerWidthDp)
    TableCell(text = second, weight = 19, containerWidthDp = containerWidthDp)
    TableCell(text = third, weight = 19, containerWidthDp = containerWidthDp)
    TableCell(text = fourth, weight = 16, containerWidthDp = containerWidthDp)
    TableCell(text = fifth, weight = 18, containerWidthDp = containerWidthDp)
}

@Composable
fun TableCell(text: String, weight: Int, containerWidthDp: Dp) {
    Box(
        modifier = Modifier
            //.fillMaxWidth(weight)
            //.fillMaxSize(42.)
            .width(((containerWidthDp * weight) / 100f))
            .padding(4.dp),

        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Start,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            softWrap = true,
            lineHeight = 20.sp,
        )
    }

}