package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.services.SettingsService
import com.gitlab.sszuev.flashcards.utils.settingsContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection

class RemoteSettingsService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    private val connection: Connection,
) : SettingsService {
    constructor() : this(
        topic = ServicesConfig.settingsNatsTopic,
        requestTimeoutInMillis = ServicesConfig.mainRequestTimeoutInMilliseconds,
        connection = NatsConnector.connection
    )

    override suspend fun getSettings(context: SettingsContext): SettingsContext = context.exec()
    override suspend fun updateSettings(context: SettingsContext): SettingsContext = context.exec()

    private suspend fun SettingsContext.exec(): SettingsContext {
        val answer = connection.requestWithRetry(
            topic = topic,
            data = this@exec.toByteArray(),
            requestTimeoutInMillis = requestTimeoutInMillis,
        )
        val res = settingsContextFromByteArray(answer.data)
        res.copyTo(this)
        return this
    }

    private fun SettingsContext.copyTo(target: SettingsContext) {
        target.responseSettingsEntity = this.responseSettingsEntity
        target.errors.addAll(this.errors)
        target.status = this.status
    }
}