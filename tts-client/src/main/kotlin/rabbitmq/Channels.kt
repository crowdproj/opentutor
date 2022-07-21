package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback

internal fun Channel.exchange(exchangeName: String, exchangeType: String): Channel {
    this.exchangeDeclare(
        /* exchange = */ exchangeName,
        /* type = */ exchangeType
    )
    return this
}

internal fun Channel.queue(queueName: String): Channel {
    queueDeclare(
        /* queue = */
        queueName,
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

internal fun Channel.bind(queueName: String, exchangeName: String, routingKey: String): Channel {
    queueBind(queueName, exchangeName, routingKey)
    return this
}

internal fun Channel.consume(
    queueName: String,
    consumerTag: String,
    deliverCallback: DeliverCallback,
    cancelCallback: CancelCallback
): Channel {
    basicConsume(
        /* queue = */ queueName,
        /* autoAck = */ true,
        /* consumerTag = */ consumerTag,
        /* deliverCallback = */ deliverCallback,
        /* cancelCallback = */ cancelCallback
    )
    return this
}