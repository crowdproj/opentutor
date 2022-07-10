package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.Settings

data class QueueConfig(
    val routingKeyRequest: String = Settings.routingKeyRequest,
    val routingKeyResponsePrefix: String = Settings.routingKeyResponsePrefix,
    val exchangeName: String = Settings.exchangeName,
    val requestQueueName: String = Settings.queueNameRequest,
    val consumerTag: String = Settings.consumerTag,
)