plugins {
    kotlin("jvm") version "1.6.21" apply false
}

group = "com.gitlab.sszuev.flashcards"
version = "2.0.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
