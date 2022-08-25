package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.ServerSettings
import com.rabbitmq.client.ConnectionFactory

data class ConnectionConfig(
    val host: String = ServerSettings.host,
    val port: Int = ServerSettings.port,
    val user: String = ServerSettings.user,
    val password: String = ServerSettings.password,
)

fun ConnectionFactory.configure(config: ConnectionConfig): ConnectionFactory {
    this.host = config.host
    this.port = config.port
    this.username = config.user
    this.password = config.password
    return this
}