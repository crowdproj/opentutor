plugins {
    kotlin("jvm")
    id("java-test-fixtures")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val commonsCompressVersion: String by project
    val typesafeConfigVersion: String by project
    val slf4jVersion: String by project

    implementation(project(":common"))
    implementation("org.apache.commons:commons-compress:$commonsCompressVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testFixturesImplementation(project(":common"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}