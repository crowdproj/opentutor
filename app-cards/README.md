#### Cards Server

The service uses [NATS](https://nats.io/) as transport to deliver card's contexts.
TTS client is located [here](../services).

The server has the following settings (example):

```
	cards-server.nats.host           = localhost
	cards-server.nats.port           = 4222
	cards-server.nats.user           = guest
	cards-server.nats.password       = guest            
    cards-server.nats.topic          = TTS
    cards-server.nats.group          = TTS
```

#### build and run application using docker & gradle:

```shell
$ docker rm -v open-tutor-cards-server
$ docker rmi sszuev/open-tutor-cards-server:2.0.0-snapshot
$ gradle clean build dockerBuildImage
$ docker run --network tutor-deploy_default --name open-tutor-cards-server sszuev/open-tutor-cards-server:2.0.0-snapshot  
```