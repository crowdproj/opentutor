plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":db-api"))
    implementation(project(":db-pg"))
    implementation(project(":db-mem"))
    implementation(project(":tts-lib"))
    implementation(project(":tts-client"))
}