package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.Connection

/**
 * A factory for [Rabbit-MQ Connections][Connection].
 */
interface RabbitmqConnectionFactory {

    val connection: Connection
}