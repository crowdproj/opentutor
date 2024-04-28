### flashcard-kt ::: tts-client

A Rabbit MQ based client for [tts-server](../tts-server), which uses [tts-lib](../tts-lib).

The client has the following settings (example):
```
    tts-client.request-timeout-in-milliseconds  = 1000
    tts-client.nats.host                        = localhost
    tts-client.nats.port                        = 4222
    tts-client.nats.user                        = guest
    tts-client.nats.password                    = guest
    tts-client.nats.topic                       = TTS
```