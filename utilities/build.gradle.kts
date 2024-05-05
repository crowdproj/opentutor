plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val typesafeConfigVersion: String by project
    implementation("com.typesafe:config:$typesafeConfigVersion")
}