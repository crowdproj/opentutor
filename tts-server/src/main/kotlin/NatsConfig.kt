package com.gitlab.sszuev.flashcards.speaker

data class NatsConfig(
    val url: String = "nats://${ServerSettings.host}:${ServerSettings.port}",
    val user: String = ServerSettings.user,
    val password: String = ServerSettings.password,
    val topic: String = ServerSettings.topic,
    val group: String = ServerSettings.group,
)