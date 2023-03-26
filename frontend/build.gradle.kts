plugins {
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val bootstrapVersion: String by project
    val jqueryVersion: String by project
    val keycloakJsVersion: String by project

    implementation("org.webjars:bootstrap:$bootstrapVersion")
    implementation("org.webjars:jquery:$jqueryVersion")
    implementation("org.webjars.npm:keycloak-js:$keycloakJsVersion")
}