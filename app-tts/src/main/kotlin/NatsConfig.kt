package com.gitlab.sszuev.flashcards.speaker

data class NatsConfig(
    val url: String = "nats://${TTSServerSettings.natsHost}:${TTSServerSettings.natsPort}",
    val user: String = TTSServerSettings.user,
    val password: String = TTSServerSettings.password,
    val topic: String = TTSServerSettings.topic,
    val group: String = TTSServerSettings.group,
)