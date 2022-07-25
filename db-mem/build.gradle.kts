plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val slf4jVersion: String by project

    implementation(project(":config"))
    implementation(project(":common"))
    implementation(project(":db-common"))

    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation(testFixtures(project(":db-common")))
}

tasks.test {
    useJUnitPlatform()
}