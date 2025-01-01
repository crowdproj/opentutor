package com.github.sszuev.flashcards.android

import android.app.Application
import android.content.Context
import android.util.Log
import java.util.Properties

object AppConfig {
    private val properties: Properties by lazy {
        AppContextProvider.getContext().loadProperties("application.properties")
    }

    val serverUri: String by lazy {
        val res = properties.getProperty("server.uri", "http://10.0.2.2:8080")
        Log.i("AppConfig", "SERVER URI = <$res>")
        res
    }
}

private fun Context.loadProperties(resourceUri: String): Properties {
    val properties = Properties()
    assets.open(resourceUri).use { inputStream ->
        properties.load(inputStream)
    }
    return properties
}

class AppContextProvider : Application() {

    companion object {
        private lateinit var instance: AppContextProvider

        fun getContext(): Context {
            if (!this::instance.isInitialized) {
                throw IllegalStateException("AppContextProvider is not initialized")
            }
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}