plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val slf4jVersion: String by project
    val kotlinCoroutinesVersion: String by project
    val caffeineVersion: String by project

    implementation(project(":cor-lib"))
    implementation(project(":db-api"))
    implementation(project(":tts-api"))
    implementation(project(":translation-api"))
    implementation(project(":common"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation(testFixtures(project(":translation-lib")))
    testImplementation(testFixtures(project(":tts-lib")))
    testImplementation(testFixtures(project(":db-api")))
}

tasks.test {
    useJUnitPlatform()
}