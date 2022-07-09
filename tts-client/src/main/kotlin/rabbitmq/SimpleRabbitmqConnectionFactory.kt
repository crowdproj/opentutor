package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.Connection
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(SimpleRabbitmqConnectionFactory::class.java)

class SimpleRabbitmqConnectionFactory(val config: ConnectionConfig) : RabbitmqConnectionFactory {

    override val connection: Connection by lazy {
        createConnection()
    }

    private fun createConnection(): Connection {
        logger.info("Create connection $config.")
        val res = com.rabbitmq.client.ConnectionFactory()
            .apply {
                this.host = config.host
                this.port = config.port
                this.username = config.user
                this.password = config.password
            }
            .newConnection()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                logger.info("Close connection $config.")
                try {
                    res.close(599, "server shutdown", 2_000)
                } catch (ignore: AlreadyClosedException) {
                    logger.debug("Connection $config already closed.")
                }
            }
        })
        return Wrapper(res)
    }

    private class Wrapper(conn: Connection): Connection by conn {
        override fun close() {
            logger.warn("Attempt to close RabbitMQ connection")
        }

        override fun close(closeCode: Int, closeMessage: String?) {
            this.close()
        }

        override fun close(timeout: Int) {
            this.close()
        }

        override fun close(closeCode: Int, closeMessage: String?, timeout: Int) {
            this.close()
        }
    }
}