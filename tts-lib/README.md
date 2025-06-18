### flashcard-kt ::: tts-lib

The module provides text-to-speech functionality for voicing words:
it accepts resource identifier and returns byte array (audio data, wav).

The resource identifier must be in the format `lang:word`, where `lang` depends on underlying mechanism,
but usually it is well-known language tag (`en`, `fr`, etc).  

Currently, there are the following three implementations of TTS service :

- [LocalTextToSpeechService](/src/main/kotlin/impl/LocalTextToSpeechService.kt) - for testing;
  can work with local tar archives in the shtooka format. See also https://shtooka.net/ - a collection of audio-resources.

- [VoicerssTextToSpeechService](/src/main/kotlin/impl/VoicerssTextToSpeechService.kt) - a client for http://www.voicerss.org/ service.   
  to make it work please obtain voice-rss-api key and specify it using vm-option `tts.service.voicerss.key`.
  For testing and local usage only.

- [GoogleTextToSpeechService](/src/main/kotlin/impl/GoogleTextToSpeechService.kt) - a client for Google TTS service.   
  google-key.json is required.

- [EspeakNgTestToSpeechService](src/main/kotlin/impl/EspeakNgTestToSpeechService.kt) - enjoy mechanical voice provided by [espeak-ng](https://github.com/espeak-ng/espeak-ng). 
There is also ubuntu image with preinstalled espeak-ng (`docker pull sszuev/ubuntu-jammy-openjdk-17-espeak-ng`), see https://hub.docker.com/repository/docker/sszuev/ubuntu
