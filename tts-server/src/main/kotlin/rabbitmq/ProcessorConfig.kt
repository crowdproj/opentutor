package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.ServerSettings

data class ProcessorConfig(
    val routingKeyRequest: String = ServerSettings.routingKeyRequest,
    val routingKeyResponsePrefix: String = ServerSettings.routingKeyResponsePrefix,
    val exchangeName: String = ServerSettings.exchangeName,
    val requestQueueName: String = ServerSettings.queueNameRequest,
    val consumerTag: String = ServerSettings.consumerTag,
    val messageSuccessResponsePrefix: String = ServerSettings.messageSuccessResponsePrefix,
    val messageErrorResponsePrefix: String = ServerSettings.messageErrorResponsePrefix,
    val messageStatusHeader: String = ServerSettings.messageStatusHeader,
)