plugins {
    kotlin("multiplatform")
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm {}

    sourceSets {
        val kotlinDatetimeVersion: String by project
        val commonMain by getting {
            dependencies {
                implementation(project(":db-common"))
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDatetimeVersion")
            }
        }
    }
}