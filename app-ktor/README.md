### flashcard-kt ::: app-ktor

The Ktor-based web-application for learning foreign words.

For development purposes there is a possibility to run application without keycloak-authorization on in-memory non-persistent sample database.   
See run example below.
To run with authorization and postgres-db please run corresponding docker containers (see [tutor-deploy dir](../tutor-deploy/README.md)).

#### Requirements:
- java-11+
- gradle-7+
- docker

#### Build and run (example)

```shell
$ docker rm -v flashcards-kotlin-app
$ docker image rm flashcards-kotlin-app:latest
$ cd app-ktor
$ gradle clean build dockerCreateDockerfile
$ docker build -f build/docker/Dockerfile -t flashcards-kotlin-app build/docker
$ docker run --name flashcards-kotlin-app -e KEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6 -e RUN_MODE=test -p 8080:8080 flashcards-kotlin-app
```