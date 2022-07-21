plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val slf4jVersion: String by project
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
}