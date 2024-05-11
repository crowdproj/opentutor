plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm {}

    sourceSets {
        val kotlinDatetimeVersion: String by project
        val kotlinxSerializationVersion: String by project
        val commonMain by getting {
            dependencies {
                implementation(project(":db-api"))
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDatetimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                val junitVersion: String by project
                val kotlinVersion: String by project
                implementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
                implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
            }
        }
    }
}