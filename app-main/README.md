### flashcard-kt ::: app-main

The ktor-based web-application for composing custom dictionaries and learning words via flashcards.

#### Standalone App

This is a main application server, which, in prod, connects to other services (cards, tts, etc.) via NATs.
There is also a standalone edition for local single-user running,
which does not require any extra services and dependencies.         
The user dictionaries and cards data is located in the directory `/app/userdata`,
which can be mounted as a docker-volume.           
By default, [espeak-ng](https://github.com/espeak-ng/espeak-ng) is used as Text-To-Speech service
(it is pre-installed inside `sszuev/ubuntu-jammy-openjdk-17-espeak-ng`).        
Also, it is possible to use [voicerss](https://www.voicerss.org/api/) TTS service (better quality):
get API-key and specify it as an environment variable `VOICERSS-KEY`,
e.g. `docker run ... -e VOICERSS-KEY=${your-key} ...`

#### Prod App

The prod version uses google TTS & translation and yandex translation services and consists of several microservices:
cards, dictionaries, settings, tts, translation, etc.
For more details see [tutor-deploy dir](../tutor-deploy/README.md).

#### run standalone application from dockerhub (pre-build) image:
```shell
$ docker pull sszuev/open-tutor-standalone:latest
$ docker rm -v open-tutor-app
$ docker run --name open-tutor-app -p 8080:8080 sszuev/open-tutor-standalone:latest
```

#### build and run standalone application using docker & gradle:
```shell
$ docker rm -v open-tutor-app
$ docker rmi sszuev/open-tutor-standalone:latest
$ gradle clean build dockerBuildImage -Dstandalone=true
$ docker run --name open-tutor-app -p 8080:8080 sszuev/open-tutor-standalone:latest  
```

#### build and run prod application using docker & gradle:

```shell
$ cd ../tutor-deploy
$ ./build-images.sh
$ docker-compose -f docker-compose-app.yml up  
```

#### run via IDE

The following VM options can be used to run the standalone version through IDE:

```
-DBOOTSTRAP_SERVERS=LOGS_KAFKA_HOSTS_IS_UNDEFINED
-DKEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6
-DVOICERSS_KEY=<your-key>
-DRUN_MODE=test
-DDATA_DIRECTORY=<directory-with-dictionaries>
-DAPP_LOG_LEVEL=debug
```

Where

- `-DRUN_MODE=test` - run standalone version
- `-DBOOTSTRAP_SERVERS=LOGS_KAFKA_HOSTS_IS_UNDEFINED` - disable kafka logging
- `-DKEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6` - backdoor to turn off authorization
- `-DVOICERSS_KEY=<your-key>` - if not specified, sample data or espeak-ng (if installed) will be used
- `-DDATA_DIRECTORY=<directory-with-dictionaries>` - if not specified, dictionaries are taken from class-path
  (`app-main/src/main/resources/data`, read-only access).
- `-DAPP_LOG_LEVEL=debug` app log level (console)

#### After build and run, the application will be available via http://localhost:8080