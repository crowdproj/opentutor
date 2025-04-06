package com.github.sszuev.flashcards.android.utils

import android.app.Activity.MODE_PRIVATE
import android.content.Context

fun Context.username(): String {
    val prefs = getSharedPreferences("auth", MODE_PRIVATE)
    val accessToken = prefs.getString("access_token", null)
    return getUsernameFromToken(accessToken)
}