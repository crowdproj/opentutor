#### Dictionaries Server

The service uses [NATS](https://nats.io/) as transport to deliver dictionaries' contexts.
TTS client is located [here](../services).

The server has the following settings (example):

```
	dictionaries-server.nats.host           = localhost
	dictionaries-server.nats.port           = 4222
	dictionaries-server.nats.user           = guest
	dictionaries-server.nats.password       = guest            
    dictionaries-server.nats.topic          = DICTIONARIES
    dictionaries-server.nats.group          = DICTIONARIES
```

#### build and run application using docker & gradle:

```shell
$ docker rm -v open-tutor-dictionaries-server
$ docker rmi sszuev/open-tutor-dictionaries-server:2.0.0-snapshot
$ gradle clean build dockerBuildImage
$ docker run --network tutor-deploy_default --name open-tutor-dictionaries-server sszuev/open-tutor-dictionaries-server:2.0.0-snapshot  
```