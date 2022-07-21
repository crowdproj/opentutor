#### Text To Speech Server

A module that responsible for the voicing words: 
it accepts resource identifier and returns byte array (audio data, wav).
The service uses [RabbitMQ](https://www.rabbitmq.com/) as transport. 

The server has the following settings (example):
```
	rabbitmq-host                  = localhost
	rabbitmq-port                  = 5672
	rabbitmq-user                  = guest
	rabbitmq-password              = guest            
	routing-key-request            = resource-identifiers
	routing-key-response-prefix    = resource-body=
	queue-name-request             = tts-queue
	consumer-tag                   = tts-server-consumer
	exchangeName                   = tts-exchange
	message-success-prefix         = response-success=  
	message-error-prefix           = response-error=
	message-status-header          = status
```

- the exchange-type is always 'direct'.
- the parameter `routing-key-request` describes a routing key for MQ queue to receive request from client (see [TTS-client](../tts-client)),
- the parameter `routing-key-response-prefix` describes a routing key prefix for queue to send to response data (i.e. byte array), 
the whole key would be `routing-key-response-prefix` + `resourceId`.

______
A first version of TTS-service is located here: https://gitlab.com/sszuev/flashcards/-/tree/master/speaker
