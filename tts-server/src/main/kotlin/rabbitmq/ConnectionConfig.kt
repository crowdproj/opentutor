package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.Settings
import com.rabbitmq.client.ConnectionFactory

data class ConnectionConfig(
    val host: String = Settings.host,
    val port: Int = Settings.port,
    val user: String = Settings.user,
    val password: String = Settings.password,
)

fun ConnectionFactory.configure(config: ConnectionConfig): ConnectionFactory {
    this.host = config.host
    this.port = config.port
    this.username = config.user
    this.password = config.password
    return this
}