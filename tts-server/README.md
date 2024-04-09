#### Text To Speech Server

The service uses [RabbitMQ](https://www.rabbitmq.com/) as transport to deliver audio-resources.
It uses [tts-lib](../tts-lib).  
TTS client is located [here](../tts-client). 

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

There are three launch options here:

- espeak-ng (if it is available locally)
- pre-build archive with sounds (see [weather.tar](src/main/resources/data/en/weather.tar))
- voice-rss (need to obtain a free API key)

#### build and run application using docker & gradle:

```shell
$ docker rm -v open-tutor-tts-server
$ docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
$ gradle clean build buildTTSServerDockerImage
$ docker run --network tutor-deploy_default --name open-tutor-tts-server sszuev/open-tutor-tts-server:2.0.0-snapshot  
```

______
A first version of TTS-service is located here: https://gitlab.com/sszuev/flashcards/-/tree/master/speaker
