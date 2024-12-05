#### Translation Server

The service uses [NATS](https://nats.io/) as transport to deliver translation-resources.
It uses [translation-lib](../translation-lib).  
TTS client is located [here](../services).

The server has the following settings (example):

```
	translation-server.nats.host           = localhost
	translation-server.nats.port           = 4222
	translation-server.nats.user           = guest
	translation-server.nats.password       = guest            
    translation-server.nats.topic          = TTS
    translation-server.nats.group          = TTS
```

#### build and run application using docker & gradle:

```shell
$ docker rm -v open-tutor-translation-server
$ docker rmi sszuev/open-tutor-translation-server:2.0.0-snapshot
$ gradle clean build dockerBuildImage
$ docker run --network tutor-deploy_default --name open-tutor-translation-server sszuev/open-tutor-translation-server:2.0.0-snapshot  
```