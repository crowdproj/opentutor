plugins {
    kotlin("jvm")
    id("application")
    id("java-test-fixtures")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val kotlinCoroutinesVersion: String by project
    val rabbitVersion: String by project
    val logbackVersion: String by project
    val junitVersion: String by project
    val slf4jVersion: String by project
    val testContainersVersion: String by project
    val mockkVersion: String by project

    implementation(project(":config"))
    implementation(project(":common"))

    implementation("com.rabbitmq:amqp-client:$rabbitVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.testcontainers:rabbitmq:$testContainersVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testFixturesImplementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}