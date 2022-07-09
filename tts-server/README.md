#### Text To Speech Server

A module that responsible for the voicing words: 
it accepts resource identifier and returns byte array (audio data, wav).
The service uses [RabbitMQ](https://www.rabbitmq.com/) as transport. 

The default settings:
```
	routingKeyIn   = resource-identifier
	routingKeyOut  = resource-body
	queueName      = tts-queue
	consumerTag    = tts-server-consumer
	exchangeName   = tts-exchange
	exchangeType   = direct
```
where `routingKeyIn` - a name of topic to send request, `routingKeyOut` - a name of topic to receive data.  

A first version is located here: https://gitlab.com/sszuev/flashcards/-/tree/master/speaker
