### flashcard-kt ::: app-ktor

The Ktor-based web-application for learning foreign words.

For development purposes there is a possibility to run application without keycloak-authorization on in-memory non-persistent sample database (demo mode).   
See run example below.
For prod mode please run corresponding docker containers (see [tutor-deploy dir](../tutor-deploy/README.md)).

#### Requirements:
- java-11+
- gradle-7+
- docker
- docker-compose

#### Build and run demo 
##### using docker:

```shell
$ docker rm -v open-tutor-app
$ docker image rm sszuev/open-tutor:latest
$ cd app-ktor
$ gradle clean build dockerCreateDockerfile
$ docker build -f build/docker/Dockerfile -t sszuev/open-tutor build/docker
$ docker run --name open-tutor-app -e KEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6 -e RUN_MODE=test -p 8080:8080 sszuev/open-tutor
```
##### using image:  
```shell
$ docker rm -v open-tutor-demo-app
$ gradle clean build dockerBuildImage -Ddemo=true
$ docker run --name open-tutor-demo-app -p 8080:8080 sszuev/open-tutor-demo
```