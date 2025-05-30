plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("java-test-fixtures")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val typesafeConfigVersion: String by project
    val ktorVersion: String by project
    val kotlinxSerializationVersion: String by project
    val slf4jVersion: String by project
    val googleTranslateVersion: String by project
    val caffeineVersion: String by project

    implementation(project(":utilities"))
    implementation(project(":translation-api"))
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:${caffeineVersion}")
    api("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.google.cloud:google-cloud-translate:$googleTranslateVersion")

    testFixturesImplementation(project(":translation-api"))
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.processResources {
    from("../tutor-deploy") {
        include("google-key.json")
    }
}

tasks.test {
    useJUnitPlatform()
}