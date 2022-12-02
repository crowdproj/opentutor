plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val kotlinCoroutinesVersion: String by project

    implementation(project(":cor-lib"))
    implementation(project(":common"))
    implementation(project(":stubs"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation(testFixtures(project(":tts-lib")))
    testImplementation(testFixtures(project(":db-common")))
}

tasks.test {
    useJUnitPlatform()
}