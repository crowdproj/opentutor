package com.gitlab.sszuev.flashcards.config

import com.gitlab.sszuev.flashcards.model.common.AppMode
import io.ktor.server.config.ApplicationConfig

data class RunConfig(
    val auth: String,
    val mode: Mode,
) {

    constructor(config: ApplicationConfig) : this(
        auth = config.property("run-config.debug-auth").getString(),
        mode = Mode.valueOf(config.property("run-config.mode").getString().uppercase()),
    )

    fun modeString() = when (mode) {
        Mode.PROD -> AppMode.PROD
        Mode.TEST -> AppMode.TEST
    }.name.lowercase()

    enum class Mode {
        PROD, TEST
    }
}