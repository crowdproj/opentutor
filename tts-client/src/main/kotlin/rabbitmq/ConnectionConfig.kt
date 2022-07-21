package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.Settings

data class ConnectionConfig(
    val host: String = Settings.host,
    val port: Int = Settings.port,
    val user: String = Settings.user,
    val password: String = Settings.password,
) {
    override fun toString(): String {
        return "(connection=<$host:$port>)"
    }
}