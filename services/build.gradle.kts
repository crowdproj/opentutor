plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val testContainersVersion: String by project
    val typesafeConfigVersion: String by project
    val natsVersion: String by project
    val slf4jVersion: String by project
    val junitVersion: String by project
    val kotlinCoroutinesVersion: String by project

    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":db-api"))
    implementation(project(":db-pg"))
    implementation(project(":db-mem"))
    implementation(project(":tts-lib"))
    implementation(project(":utilities"))

    implementation("io.nats:jnats:$natsVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
}

tasks.test {
    useJUnitPlatform()
}