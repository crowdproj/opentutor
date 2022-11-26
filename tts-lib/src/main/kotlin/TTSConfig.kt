package com.gitlab.sszuev.flashcards.speaker

data class TTSConfig(
    val httpClientConnectTimeoutMs: Long = TTSSettings.httpClientConnectTimeoutMs,
    val httpClientRequestTimeoutMs: Long = TTSSettings.httpClientRequestTimeoutMs,
    val ttsServiceVoicerssApi: String = TTSSettings.ttsServiceVoicerssApi,
    val ttsServiceVoicerssKey: String = TTSSettings.ttsServiceVoicerssKey,
    val ttsServiceVoicerssFormat: String = TTSSettings.ttsServiceVoicerssFormat,
    val ttsServiceVoicerssCodec: String = TTSSettings.ttsServiceVoicerssCodec,
)