#### Text To Speech Server

The service uses [NATS](https://nats.io/) as transport to deliver audio-resources.
It uses [tts-lib](../tts-lib).  
TTS client is located [here](../tts-client). 

The server has the following settings (example):
```
	tts-server.nats.host           = localhost
	tts-server.nats.port           = 4222
	tts-server.nats.user           = guest
	tts-server.nats.password       = guest            
    tts-server.nats.topic          = TTS
    tts-server.nats.group          = TTS
```

There are three launch options here:

- espeak-ng (if it is available locally)
- pre-build archive with sounds (see [weather.tar](src/main/resources/data/en/weather.tar))
- voice-rss (need to obtain a free API key)

#### build and run application using docker & gradle:

```shell
$ docker rm -v open-tutor-tts-server
$ docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
$ gradle clean build dockerBuildImage
$ docker run --network tutor-deploy_default --name open-tutor-tts-server sszuev/open-tutor-tts-server:2.0.0-snapshot  
```

______
A first version of TTS-service is located here: https://gitlab.com/sszuev/flashcards/-/tree/master/speaker
