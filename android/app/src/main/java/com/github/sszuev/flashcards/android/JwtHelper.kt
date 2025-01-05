package com.github.sszuev.flashcards.android

import android.util.Base64
import android.util.Log
import org.json.JSONObject

private const val tag = "JwtHelper"

fun getUsernameFromToken(token: String?): String {
    if (token == null) return "Unknown User"
    return try {
        val json = parseJwtPayload(token)
        json.optString("name", "Unknown User")
    } catch (e: Exception) {
        Log.e(tag, "get-user-name", e)
        "Unknown User"
    }
}

fun parseJwtPayload(jwt: String): JSONObject {
    val parts = jwt.split(".")
    if (parts.size < 2) {
        throw IllegalArgumentException("Invalid JWT: Payload missing")
    }
    val payload = decodeBase64Url(parts[1])
    return JSONObject(payload)
}

fun decodeBase64Url(encoded: String): String {
    return String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
}

