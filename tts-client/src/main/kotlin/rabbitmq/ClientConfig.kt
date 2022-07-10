package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.Settings

data class ClientConfig(
    val routingKeyRequest: String = Settings.routingKeyRequest,
    val routingKeyResponsePrefix: String = Settings.routingKeyResponsePrefix,
    val exchangeName: String = Settings.exchangeName,
    val consumerTag: String = Settings.consumerTag,
    val messageStatusHeader: String = Settings.messageStatusHeader,
)