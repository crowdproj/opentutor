plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val kotlinCoroutinesVersion: String by project
    val natsVersion: String by project
    val ktorVersion: String by project
    val slf4jVersion: String by project

    implementation("io.nats:jnats:$natsVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation(project(":db-pg"))
}