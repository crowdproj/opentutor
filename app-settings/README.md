#### Settings Server

The service uses [NATS](https://nats.io/) as transport to deliver settings' contexts.

The server has the following settings (example):

```
	settings-server.nats.host           = localhost
	settings-server.nats.port           = 4222
	settings-server.nats.user           = guest
	settings-server.nats.password       = guest            
    settings-server.nats.topic          = DICTIONARIES
    settings-server.nats.group          = DICTIONARIES
```

#### build and run application using docker & gradle:

```shell
$ docker rm -v open-tutor-settings-server
$ docker rmi sszuev/open-tutor-settings-server:2.0.0-snapshot
$ gradle clean build dockerBuildImage
$ docker run --network tutor-deploy_default --name open-tutor-settings-server sszuev/open-tutor-settings-server:2.0.0-snapshot  
```