plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val logbackVersion: String by project
    val logstashLogbackEncoderVersion: String by project
    val kotlinCoroutinesVersion: String by project
    val junitVersion: String by project

    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    api("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}