rootProject.name = "flashcards-kt"

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        val openapiVersion: String by settings
        val bmuschkoVersion: String by settings

        kotlin("jvm") version kotlinVersion apply false
        id("org.openapi.generator") version openapiVersion apply false
        id("com.bmuschko.docker-java-application") version bmuschkoVersion apply false
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
    }
}

include("openapi")
include("common")
include("mappers")
include("app-ktor")
include("cor-lib")
include("core")
include("services")
include("tts-server")
include("tts-client")
include("db-mem")
include("db-api")
include("db-pg")
include("logs-openapi")
include("logs-mappers")
include("logs-lib")
include("tts-lib")
include("frontend")
