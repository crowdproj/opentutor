## OpenTutor - a service for compiling user dictionaries and learning foreign words

A simple open-source kotlin-ktor web-application for compiling custom dictionaries and learning foreign words through flashcards.         

#### Edit dictionaries:

![edit dictionary](./flashcards-edit.gif)

#### Run flashcards:

![run flashcards](./flashcards-run.gif)

#### Summary:

It is supposed to be an extended analogue of the desktop program *Lingvo Tutor*, which is a component of well-known [ABBYY Lingvo](https://www.lingvo.ru/multi/).

At the moment, the application is only available as a docker image.
The userdata with built-in dictionaries is stored in the `/userdata` directory (inside container), this allows to use docker volumes.

By default, [espeak-ng](https://github.com/espeak-ng/espeak-ng) is used as Text-To-Speech service (it is pre-installed inside `sszuev/ubuntu-jammy-openjdk-17-espeak-ng` image).        
To use [voicerss](https://www.voicerss.org/api/) TTS service (better quality)
obtain API-key and specify it as an environment variable `VOICERSS-KEY`, e.g. `docker run ... -e VOICERSS-KEY=${your-key} ...`

This is a second edition, the first one is a java-spring application, which home is [here](https://gitlab.com/sszuev/flashcards)

#### Requirements (for dev): 
- java-17+
- gradle-7+
- docker

#### Build and run:

- `$ docker pull sszuev/open-tutor-standalone:latest && docker run --name open-tutor-standalone-app -p 8080:8080 sszuev/open-tutor-standalone:latest`
- [app-ktor/README](./app-ktor/README.md)

#### License:
- Apache License Version 2.0
