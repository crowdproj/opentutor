plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val kotlinCoroutinesVersion: String by project
    val natsVersion: String by project
    val logbackVersion: String by project
    val junitVersion: String by project
    val slf4jVersion: String by project
    val testContainersVersion: String by project
    val typesafeConfigVersion: String by project

    implementation(project(":common"))
    implementation(project(":tts-lib"))

    implementation("io.nats:jnats:$natsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
}

tasks.test {
    useJUnitPlatform()
}