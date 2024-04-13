package com.gitlab.sszuev.flashcards.config

import io.ktor.server.config.ApplicationConfig

data class RunConfig(
    val auth: String,
    val mode: Mode,
) {

    constructor(config: ApplicationConfig) : this(
        auth = config.property("run-config.debug-auth").getString(),
        mode = Mode.valueOf(config.property("run-config.mode").getString().uppercase()),
    )

    enum class Mode {
        PROD, TEST
    }
}