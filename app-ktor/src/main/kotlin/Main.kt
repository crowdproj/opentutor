package com.gitlab.sszuev.flashcards

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gitlab.sszuev.flashcards.api.cardApiV1
import com.gitlab.sszuev.flashcards.api.dictionaryApiV1
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.KeycloakConfig
import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.config.TutorConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.oauth
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.Locations
import io.ktor.server.locations.location
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.param
import io.ktor.server.routing.routing
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import io.ktor.server.webjars.Webjars
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.title
import org.slf4j.event.Level
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.nio.charset.StandardCharsets
import java.util.Base64

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.MainKt")

// use to jetty, not netty, due to exception https://youtrack.jetbrains.com/issue/KTOR-4433
fun main(args: Array<String>) = io.ktor.server.jetty.EngineMain.main(args)

/**
 * Backdoors for developing:
 * - To disable ElK-logging use `-DBOOTSTRAP_SERVERS=LOGS_KAFKA_HOSTS_IS_UNDEFINED` (or empty string)
 * - To enable ELK-logging use `-DBOOTSTRAP_SERVERS=localhost:9094`
 * - To disable authentication for debugging use `-DKEYCLOAK_DEBUG_AUTH=auth-uuid`
 * - For run mode (prod, test, stub) use `-DRUN_MODE=mode`
 *
 * Example: `-DBOOTSTRAP_SERVERS=LOGS_KAFKA_HOSTS_IS_UNDEFINED -DKEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6 -DRUN_MODE=test`
 */
@KtorExperimentalLocationsAPI
@Suppress("unused")
fun Application.module(
    keycloakConfig: KeycloakConfig = KeycloakConfig(environment.config),
    runConfig: RunConfig = RunConfig(environment.config),
    tutorConfig: TutorConfig = TutorConfig(environment.config),
) {
    logger.info(printGeneralSettings(runConfig, keycloakConfig, tutorConfig))

    val repositories = appRepositories()

    val port = environment.config.property("ktor.deployment.port").getString()

    val keycloakProvider = OAuthServerSettings.OAuth2ServerSettings(
        name = "keycloak",
        authorizeUrl = "${keycloakConfig.address}/auth/realms/${keycloakConfig.realm}/protocol/openid-connect/auth",
        accessTokenUrl = "${keycloakConfig.address}/auth/realms/${keycloakConfig.realm}/protocol/openid-connect/token",
        clientId = keycloakConfig.clientId,
        clientSecret = keycloakConfig.secret,
        accessTokenRequiresBasicAuth = false,
        requestMethod = HttpMethod.Post,
        defaultScopes = listOf("roles")
    )

    install(Webjars)

    // must be before install(Authentication)
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = StandardCharsets.UTF_8.name()
        })
    }

    install(Routing)

    install(CachingHeaders)
    install(DefaultHeaders)
    install(AutoHeadResponse)

    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        this.allowCredentials = true
    }

    if (runConfig.auth.isBlank()) {
        install(Authentication) {
            oauth("keycloakOAuth") {
                client = HttpClient(Apache)
                providerLookup = { keycloakProvider }
                urlProvider = { "http://localhost:$port/" }
            }

            jwt("auth-jwt") {
                realm = keycloakConfig.realm
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(Base64.getUrlDecoder().decode(keycloakConfig.secret)))
                        .withClaimPresence("sub")
                        .build()
                )
                validate { jwtCredential: JWTCredential ->
                    JWTPrincipal(jwtCredential.payload)
                }
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(SerializationFeature.INDENT_OUTPUT)
            writerWithDefaultPrettyPrinter()
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            registerModule(JavaTimeModule())
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(Locations)

    val contextConfig = ContextConfig(runConfig, tutorConfig)
    val cardService = cardService()
    val dictionaryService = dictionaryService()

    routing {
        staticResources(remotePath = "/static", basePackage = "static") {}

        if (runConfig.auth.isBlank()) {
            authenticate("auth-jwt") {
                this@authenticate.cardApiV1(
                    service = cardService,
                    repositories = repositories,
                    contextConfig = contextConfig,
                )
                this@authenticate.dictionaryApiV1(
                    service = dictionaryService,
                    repositories = repositories,
                    contextConfig = contextConfig,
                )
            }
            authenticate("keycloakOAuth") {
                location<Index> {
                    param("error") {
                        handle {
                            call.loginFailed(call.parameters.getAll("error").orEmpty())
                        }
                    }
                    handle {
                        val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                        if (principal == null) {
                            call.respond(HttpStatusCode.Unauthorized)
                        } else {
                            call.respond(thymeleafContent(runConfig, tutorConfig, keycloakConfig, principal))
                        }
                    }
                }
            }
        } else {
            cardApiV1(
                service = cardService,
                repositories = repositories,
                contextConfig = contextConfig,
            )
            dictionaryApiV1(
                service = dictionaryService,
                repositories = repositories,
                contextConfig = contextConfig,
            )
            get("/") {
                call.respond(thymeleafContent(runConfig, tutorConfig, keycloakConfig, null))
            }
        }
    }
}

private fun thymeleafContent(
    runConfig: RunConfig,
    tutorConfig: TutorConfig,
    keycloakConfig: KeycloakConfig,
    principal: OAuthAccessTokenResponse.OAuth2?
): ThymeleafContent {
    val res = mutableMapOf<String, String>()
    val userConfig = if (principal == null) {
        mapOf(
            "user" to "dev",
            "devMode" to "true",
        )
    } else {
        mapOf(
            "user" to principal.name(),
            "keycloakAuthURL" to "${keycloakConfig.address}/auth",
            "keycloakAppRealm" to keycloakConfig.realm,
            "keycloakAppClient" to keycloakConfig.clientId,
        )
    }
    val commonConfig = mapOf(
        "runMode" to runConfig.modeString(),
        "numberOfWordsToShow" to tutorConfig.numberOfWordsToShow.toString(),
        "numberOfWordsPerStage" to tutorConfig.numberOfWordsPerStage.toString(),
        "numberOfRightAnswers" to tutorConfig.numberOfRightAnswers.toString(),
        "numberOfOptionsPerWord" to tutorConfig.numberOfOptionsPerWord.toString(),
    )
    res.putAll(userConfig)
    res.putAll(commonConfig)
    return ThymeleafContent("index", res)
}

private fun OAuthAccessTokenResponse.OAuth2?.name(): String {
    if (null == this) {
        return "Unauthorized"
    }
    val jwtToken = accessToken
    val token = JWT.decode(jwtToken)
    return token.getClaim("name").asString() ?: "Noname"
}

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("/")
class Index

private suspend fun ApplicationCall.loginFailed(errors: List<String>) {
    respondHtml {
        head {
            title { +"Login error" }
        }
        body {
            h1 {
                +"Errors"
            }
            for (e in errors) {
                p {
                    +e
                }
            }
        }
    }
}

private fun Application.printGeneralSettings(
    runConfig: RunConfig,
    keycloakConfig: KeycloakConfig,
    tutorConfig: TutorConfig
): String {
    val bootstrapServices = System.getProperty("BOOTSTRAP_SERVERS")
    val port = environment.config.property("ktor.deployment.port").getString()
    return """
            |
            |bootstrap-services             = $bootstrapServices
            |run-config-app-mode            = ${runConfig.mode}
            |run-config-debug-auth          = ${runConfig.auth}
            |keycloak-address               = ${keycloakConfig.address}
            |keycloak-realm                 = ${keycloakConfig.realm}
            |keycloak-client-id             = ${keycloakConfig.clientId}
            |application-port               = $port
            |=====================================================================
            |numberOfWordsToShow            = ${tutorConfig.numberOfWordsToShow}
            |numberOfWordsPerStage          = ${tutorConfig.numberOfWordsPerStage}
            |numberOfRightAnswers           = ${tutorConfig.numberOfRightAnswers}
            |numberOfOptionsPerWord         = ${tutorConfig.numberOfOptionsPerWord}
            """.replaceIndentByMargin("\t")
}