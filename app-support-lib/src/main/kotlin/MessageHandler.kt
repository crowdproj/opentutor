package com.gitlab.sszuev.flashcards.nats

import io.nats.client.Connection
import io.nats.client.Message

interface MessageHandler {

    suspend fun handleMessage(connection: Connection, msg: Message)
}