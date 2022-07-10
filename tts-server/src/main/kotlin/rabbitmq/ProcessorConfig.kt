package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.Settings

data class ProcessorConfig(
    val routingKeyRequest: String = Settings.routingKeyRequest,
    val routingKeyResponsePrefix: String = Settings.routingKeyResponsePrefix,
    val exchangeName: String = Settings.exchangeName,
    val requestQueueName: String = Settings.queueNameRequest,
    val consumerTag: String = Settings.consumerTag,
    val messageSuccessResponsePrefix: String = Settings.messageSuccessResponsePrefix,
    val messageErrorResponsePrefix: String = Settings.messageErrorResponsePrefix,
    val messageStatusHeader: String = Settings.messageStatusHeader,
)