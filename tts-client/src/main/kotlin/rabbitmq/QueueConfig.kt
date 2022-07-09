package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.Settings

data class QueueConfig(
    val routingKeyIn: String = Settings.routingKeyIn,
    val routingKeyOut: String = Settings.routingKeyOut,
    val exchangeName: String = Settings.exchangeName,
    val queueName: String = Settings.queueName,
    val consumerTag: String = Settings.consumerTag,
    val exchangeType: String = Settings.exchangeType,
)