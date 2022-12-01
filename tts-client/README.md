### flashcard-kt ::: tts-client

A Rabbit MQ based client for [tts-server](../tts-server), which uses [tts-lib](../tts-lib).

The client has the following settings (example):
```
	rabbitmq-host                  = localhost
	rabbitmq-port                  = 5672
	rabbitmq-user                  = guest
	rabbitmq-password              = guest          
	routing-key-request            = resource-identifiers
	routing-key-response-prefix    = resource-body=
	consumer-tag                   = tts-server-consumer
	exchange-name                  = tts-exchange
	request-timeout-in-ms          = 1000
```
- the exchange-type is always 'direct'.
- the parameter `routing-key-request` describes a routing key for queue to send request to server,
- the parameter `routing-key-response-prefix` describes a routing key prefix for queue to receive response data (i.e. byte array),
  the whole key would be `routing-key-response-prefix` + `resourceId`
- the parameter `request-timeout-in-ms` can be negative for unblocking behaviour
