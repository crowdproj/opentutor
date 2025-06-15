package com.github.sszuev.flashcards.android

import android.app.Application

class MainApplication : Application() {
    override fun onTerminate() {
        super.onTerminate()
        defaultHttpClient.close()
        lightHttpClient.close()
    }
}