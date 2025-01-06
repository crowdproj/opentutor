package com.github.sszuev.flashcards.android.utils

import android.app.Activity
import android.app.Activity.MODE_PRIVATE

fun getUsernameFromPreferences(activity: Activity): String {
    val prefs = activity.getSharedPreferences("auth", MODE_PRIVATE)
    val accessToken = prefs.getString("access_token", null)
    return getUsernameFromToken(accessToken)
}