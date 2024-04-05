plugins {
    kotlin("jvm")
    id("java-test-fixtures")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val slf4jVersion: String by project
    val typesafeConfigVersion: String by project
    val jacksonVersion: String by project

    implementation(project(":common"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testFixturesImplementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}