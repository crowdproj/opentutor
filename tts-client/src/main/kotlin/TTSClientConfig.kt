package com.gitlab.sszuev.flashcards.speaker

data class TTSClientConfig(
    val routingKeyRequest: String = TTSClientSettings.routingKeyRequest,
    val routingKeyResponsePrefix: String = TTSClientSettings.routingKeyResponsePrefix,
    val exchangeName: String = TTSClientSettings.exchangeName,
    val consumerTag: String = TTSClientSettings.consumerTag,
    val messageStatusHeader: String = TTSClientSettings.messageStatusHeader,
)