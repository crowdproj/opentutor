plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val junitVersion: String by project
    val slf4jVersion: String by project
    val exposedVersion: String by project
    val postgresDriverVersion: String by project
    val testContainersVersion: String by project
    val liquibaseVersion: String by project
    val hikariCPVersion: String by project
    val logbackVersion: String by project
    val typesafeConfigVersion: String by project

    implementation(project(":common"))
    implementation(project(":db-common"))

    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation(testFixtures(project(":db-common")))
}

tasks.test {
    useJUnitPlatform()
}