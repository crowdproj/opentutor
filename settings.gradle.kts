rootProject.name = "flashcards-kt"

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        val openapiVersion: String by settings
        val bmuschkoVersion: String by settings

        kotlin("jvm") version kotlinVersion apply false
        id("org.openapi.generator") version openapiVersion apply false
        id("com.bmuschko.docker-java-application") version bmuschkoVersion apply false
    }
}

include("openapi")
include("common")
include("mappers")
include("services")
include("app-ktor")
include("stubs")
include("corlib")
include("core")
include("config")
include("tts-server")
include("tts-client")
include("db-mem")
include("db-common")
include("db-pg")
