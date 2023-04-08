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

#### build and run application using docker & gradle:
```shell
$ docker rm -v open-tutor-app
$ gradle clean build dockerBuildImage -Dstandalone=true
$ docker run --name open-tutor-app -p 8080:8080 sszuev/open-tutor-standalone:latest  
```

#### After build and run, the application will be available via http://localhost:8080