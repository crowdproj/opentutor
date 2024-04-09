package com.gitlab.sszuev.flashcards.speaker

import com.typesafe.config.Config
import java.util.Locale

fun Config.get(key: String, default: String): String {
    val systemValue = getProperty(key)
    if (systemValue != null) {
        return systemValue
    }
    return if (hasPath(key)) getString(key) else default
}

fun Config.get(key: String, default: Int): Int {
    val systemValue = getProperty(key)
    if (systemValue != null) {
        return systemValue.toInt()
    }
    return if (hasPath(key)) getInt(key) else default
}

fun Config.get(key: String, default: Long): Long {
    val systemValue = getProperty(key)
    if (systemValue != null) {
        return systemValue.toLong()
    }
    return if (hasPath(key)) getLong(key) else default
}

private fun getProperty(key: String): String? {
    val systemPropertyValue = System.getProperty(key)
    if (systemPropertyValue != null) {
        return systemPropertyValue
    }
    val systemEnv1 = System.getenv(key)
    if (systemEnv1 != null) {
        return systemEnv1
    }
    val key2 = key.uppercase(Locale.US).replace(".", "_").replace("-", "_")
    val systemEnv2 = System.getenv(key2)
    if (systemEnv2 != null) {
        return systemEnv2
    }
    return null
}