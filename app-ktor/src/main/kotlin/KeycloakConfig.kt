package com.gitlab.sszuev.flashcards

import io.ktor.server.config.*

data class KeycloakConfig(
    val address: String,
    val clientId: String,
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
) {
    constructor(config: ApplicationConfig): this(
        address = config.property("keycloak.address").getString(),
        clientId = config.property("keycloak.clientId").getString(),
        realm = config.property("keycloak.realm").getString(),
        secret = config.property("keycloak.jwt.secret").getString(),
        issuer = config.property("keycloak.jwt.issuer").getString(),
        audience = config.property("keycloak.jwt.audience").getString(),
    )
}