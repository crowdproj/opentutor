package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.TTSClientSettings

data class ConnectionConfig(
    val host: String = TTSClientSettings.host,
    val port: Int = TTSClientSettings.port,
    val user: String = TTSClientSettings.user,
    val password: String = TTSClientSettings.password,
) {
    override fun toString(): String {
        return "(connection=<$host:$port>)"
    }
}