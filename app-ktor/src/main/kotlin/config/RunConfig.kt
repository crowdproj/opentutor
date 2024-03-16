package com.gitlab.sszuev.flashcards.config

import com.gitlab.sszuev.flashcards.model.common.AppMode
import io.ktor.server.config.ApplicationConfig

data class RunConfig(
    val auth: String,
    val mode: AppMode,
) {

    constructor(config: ApplicationConfig) : this(
        auth = config.property("run-config.debug-auth").getString(),
        mode = AppMode.valueOf(config.property("run-config.mode").getString().uppercase()),
    )

    init {
        if (mode == AppMode.PROD) {
            require(auth.isBlank()) { "No auth expected for prod mode." }
        }
    }

    fun modeString(): String {
        return mode.name.lowercase()
    }
}