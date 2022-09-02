plugins {
    kotlin("jvm")
    id("org.openapi.generator")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val jacksonVersion: String by project
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    main {
        java.srcDir("$buildDir/generate-resources/main/src/main/kotlin")
    }
}

openApiGenerate {
    val openapiGroup = "${rootProject.group}.logs"
    generatorName.set("kotlin")
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")
    inputSpec.set("$rootDir/specs/spec-logs.yml")

    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }

    configOptions.set(mapOf(
        "dateLibrary" to "string",
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
