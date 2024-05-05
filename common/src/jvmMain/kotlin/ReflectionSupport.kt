package com.gitlab.sszuev.flashcards.utils

internal actual fun createThrowable(kClassQualifiedName: String, message: String?): Throwable = try {
    val clazz = Class.forName(kClassQualifiedName)
    val constructor = clazz.getConstructor(String::class.java)
    constructor.newInstance(message) as Throwable
} catch (e: Exception) {
    Throwable("$kClassQualifiedName:::$message")
}