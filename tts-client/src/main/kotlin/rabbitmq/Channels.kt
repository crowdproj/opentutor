package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback

internal fun Channel.exchangeDeclare(config: QueueConfig): Channel {
    this.exchangeDeclare(
        /* exchange = */ config.exchangeName,
        /* type = */ config.exchangeType
    )
    return this
}

internal fun Channel.queueDeclare(config: QueueConfig): Channel {
    queueDeclare(
        /* queue = */
        config.queueName,
        /* durable = */
        false,
        /* exclusive = */
        false,
        /* autoDelete = */
        false,
        /* arguments = */
        null,
    )
    return this
}

internal fun Channel.queueBind(config: QueueConfig, routingKeyToListen: String): Channel {
    queueBind(config.queueName, config.exchangeName, routingKeyToListen)
    return this
}

internal fun Channel.basicConsume(
    config:  QueueConfig,
    deliverCallback: DeliverCallback,
    cancelCallback: CancelCallback
): Channel {
    basicConsume(
        /* queue = */ config.queueName,
        /* autoAck = */ true,
        /* consumerTag = */ config.consumerTag,
        /* deliverCallback = */ deliverCallback,
        /* cancelCallback = */ cancelCallback
    )
    return this
}