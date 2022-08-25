plugins {
    kotlin("jvm")
    id("java-test-fixtures")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val jacksonVersion: String by project
    val slf4jVersion: String by project
    val typesafeConfigVersion: String by project

    implementation(project(":common"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testFixturesImplementation(project(":common"))
}