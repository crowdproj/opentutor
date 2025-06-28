package com.gitlab.sszuev.flashcards.config

import io.ktor.server.config.ApplicationConfig

data class KeycloakConfig(
    val authorizeAddress: String,
    val accessTokenAddress: String,
    val redirectAddress: String,
    val clientId: String,
    val realm: String,
) {
    constructor(config: ApplicationConfig): this(
        authorizeAddress = config.property("keycloak.authorize-address").getString(),
        accessTokenAddress = config.property("keycloak.access-token-address").getString(),
        redirectAddress = config.property("keycloak.redirect-address").getString(),
        clientId = config.property("keycloak.clientId").getString(),
        realm = config.property("keycloak.realm").getString(),
    )
}