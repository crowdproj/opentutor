plugins {
    kotlin("jvm")
    id("org.openapi.generator")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val jacksonVersion: String by project
    val junitVersion: String by project
    implementation(platform("tools.jackson:jackson-bom:$jacksonVersion"))
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    testImplementation("tools.jackson.core:jackson-databind")
    testImplementation("tools.jackson.module:jackson-module-kotlin")
    testImplementation("tools.jackson.datatype:jackson-datatype-jsr353")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generate-resources/main/src/main/kotlin"))
    }
}

openApiGenerate {
    val openapiGroup = "${rootProject.group}.api.v1"
    generatorName.set("kotlin")
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")
    inputSpec.set("$rootDir/specs/spec-v1.yml")

    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }

    configOptions.set(mapOf(
        "enumPropertyNaming" to "UPPERCASE",
        "serializationLibrary" to "jackson",
        "collectionType" to "list"
    ))
}

tasks {
    compileKotlin {
        dependsOn(openApiGenerate)
    }
    compileTestKotlin {
        dependsOn(openApiGenerate)
    }
}
