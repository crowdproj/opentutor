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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

include("openapi")
include("common")
include("mappers")
include("app-ktor")
include("app-tts")
include("app-cards")
include("app-dictionaries")
include("app-settings")
include("app-translation")
include("services")
include("cor-lib")
include("core")
include("db-mem")
include("db-api")
include("db-pg")
include("logs-openapi")
include("logs-mappers")
include("logs-lib")
include("tts-api")
include("tts-lib")
include("translation-api")
include("translation-lib")
include("frontend")
include("utilities")