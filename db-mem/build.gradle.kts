plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val slf4jVersion: String by project
    val typesafeConfigVersion: String by project
    val commonsCSVVersion: String by project

    implementation(project(":common"))
    implementation(project(":db-api"))

    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.apache.commons:commons-csv:$commonsCSVVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation(testFixtures(project(":db-api")))
}

tasks.test {
    useJUnitPlatform()
}