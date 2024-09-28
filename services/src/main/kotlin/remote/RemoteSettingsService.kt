package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.services.NatsConnectionFactory
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.services.SettingsService
import com.gitlab.sszuev.flashcards.utils.settingsContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.temporal.ChronoUnit

class RemoteSettingsService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : SettingsService {
    constructor() : this(
        topic = ServicesConfig.settingsNatsTopic,
        requestTimeoutInMillis = ServicesConfig.requestTimeoutInMilliseconds,
        connectionFactory = { NatsConnectionFactory.connection }
    )

    private val connection: Connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }

    override suspend fun getSettings(context: SettingsContext): SettingsContext = context.exec()
    override suspend fun updateSettings(context: SettingsContext): SettingsContext = context.exec()

    private suspend fun SettingsContext.exec(): SettingsContext {
        val answer = withContext(Dispatchers.IO) {
            connection.request(
                /* subject = */ topic,
                /* body = */ this@exec.toByteArray(),
                /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
            )
        }
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