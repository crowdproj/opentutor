plugins {
    kotlin("jvm")
    id("java-test-fixtures")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    implementation(project(":common"))
    
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testFixturesImplementation(project(":common"))
}