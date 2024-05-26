### flashcard-kt ::: app-ktor

The ktor-based web-application for composing custom dictionaries and learning words via flashcards.

#### Standalone App
There is a standalone edition for local single-user running. 
This app version does not require any extra services and dependencies.         
The user dictionaries and cards data is located in the directory `/app/userdata`, which can be mounted as a docker-volume.           
By default, [espeak-ng](https://github.com/espeak-ng/espeak-ng) is used as Text-To-Speech service (it is pre-installed inside `sszuev/ubuntu-jammy-openjdk-17-espeak-ng`).        
To use [voicerss](https://www.voicerss.org/api/) TTS service (better quality) 
obtain API-key and specify it as an environment variable `VOICERSS-KEY`, e.g. `docker run ... -e VOICERSS-KEY=${your-key} ...`

#### Prod App
There is also prod mode, which requires a prepared ecosystem (see [tutor-deploy dir](../tutor-deploy/README.md)).

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
$ docker rm tutor-deploy-flashcards-tts-server-1
$ docker rm tutor-deploy-flashcards-cards-server-1
$ docker rm tutor-deploy-flashcards-dictionaries-server-1
$ docker rm tutor-deploy-flashcards-app-1
$ docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
$ docker rmi sszuev/open-tutor-cards-server:2.0.0-snapshot
$ docker rmi sszuev/open-tutor-dictionaries-server:2.0.0-snapshot
$ docker rmi sszuev/open-tutor:2.0.0-snapshot
$ cd ..
$ gradle clean build -x test
$ cd ./app-tts
$ gradle dockerBuildImage
$ cd ../app-cards
$ gradle dockerBuildImage
$ cd ../app-dictionaries
$ gradle dockerBuildImage
$ cd ../app-ktor
$ gradle dockerBuildImage
$ cd ../tutor-deploy
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
  (`app-ktor/src/main/resources/data`, read-only access).
- `-DAPP_LOG_LEVEL=debug` app log level (console)

#### After build and run, the application will be available via http://localhost:8080