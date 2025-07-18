package com.gitlab.sszuev.flashcards

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gitlab.sszuev.flashcards.api.cardApiV1
import com.gitlab.sszuev.flashcards.api.controllers.health
import com.gitlab.sszuev.flashcards.api.dictionaryApiV1
import com.gitlab.sszuev.flashcards.api.settingsApiV1
import com.gitlab.sszuev.flashcards.api.translationApiV1
import com.gitlab.sszuev.flashcards.api.ttsApiV1
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.KeycloakConfig
import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.config.TutorConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.services.LANGUAGES
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
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
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.request.uri
import io.ktor.server.resources.Resources
import io.ktor.server.resources.resource
import io.ktor.server.response.header
import io.ktor.server.response.respond
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
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.AppMainKt")

fun main(args: Array<String>) = io.ktor.server.cio.EngineMain.main(args)

/**
 * Backdoors for developing:
 * - To disable ElK-logging use `-DBOOTSTRAP_SERVERS=LOGS_KAFKA_HOSTS_IS_UNDEFINED` (or empty string)
 * - To enable ELK-logging use `-DBOOTSTRAP_SERVERS=localhost:9094`
 * - To disable authentication for debugging use `-DKEYCLOAK_DEBUG_AUTH=auth-uuid`
 * - For run mode (prod, test, stub) use `-DRUN_MODE=mode`
 *
 * Example: `-DBOOTSTRAP_SERVERS=LOGS_KAFKA_HOSTS_IS_UNDEFINED -DKEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6 -DRUN_MODE=test -DDATA_DIRECTORY=/local/data`
 */
@Suppress("unused")
fun Application.module(
    keycloakConfig: KeycloakConfig = KeycloakConfig(environment.config),
    runConfig: RunConfig = RunConfig(environment.config),
    tutorConfig: TutorConfig = TutorConfig(environment.config),
    oauthJwtVerifier: JWTVerifier = makeJwtVerifier(
        jwkUrl = "${keycloakConfig.accessTokenAddress}/realms/${keycloakConfig.realm}/protocol/openid-connect/certs",
        issuer = "${keycloakConfig.authorizeAddress}/realms/${keycloakConfig.realm}"
    ),
    keycloakSecret: String? = null,
) {
    logger.info(printGeneralSettings(runConfig, keycloakConfig, tutorConfig))

    val contextConfig = ContextConfig(runConfig, tutorConfig)
    val cardService = cardService(runConfig)
    val dictionaryService = dictionaryService(runConfig)
    val ttsService = ttsService(runConfig)
    val translationService = translationService(runConfig)
    val settingsService = settingsService(runConfig)
    val healthService = healthService(runConfig)

    val keycloakProvider = OAuthServerSettings.OAuth2ServerSettings(
        name = "keycloak",
        authorizeUrl = "${keycloakConfig.authorizeAddress}/realms/${keycloakConfig.realm}/protocol/openid-connect/auth",
        accessTokenUrl = "${keycloakConfig.accessTokenAddress}/realms/${keycloakConfig.realm}/protocol/openid-connect/token",
        clientId = keycloakConfig.clientId,
        clientSecret = keycloakSecret ?: "",
        accessTokenRequiresBasicAuth = false,
        requestMethod = HttpMethod.Post,
        defaultScopes = listOf("roles")
    )

    install(XForwardedHeaders)

    install(Webjars)

    // must be before install(Authentication)
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = StandardCharsets.UTF_8.name()
        })
    }

    install(CachingHeaders)
    install(DefaultHeaders)
    install(AutoHeadResponse)

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Origin)
        this.allowCredentials = true
    }

    if (runConfig.auth.isBlank()) {
        install(Authentication) {
            oauth("keycloakOAuth") {
                client = HttpClient(Apache)
                providerLookup = { keycloakProvider }
                urlProvider = { keycloakConfig.redirectAddress }
            }

            jwt("auth-jwt") {
                realm = keycloakConfig.realm
                verifier(oauthJwtVerifier)
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

    install(Resources)

    if (runConfig.mode == RunConfig.Mode.PROD) {
        intercept(phase = ApplicationCallPipeline.Setup) {
            val uri = call.request.uri
            if (uri.startsWith("/webjars") || uri.startsWith("/static")) {
                call.response.header(name = HttpHeaders.CacheControl, value = "public, max-age=31536642, immutable")
            }
        }
    }

    routing {
        staticResources(remotePath = "/static", basePackage = "static") {}

        if (runConfig.auth.isBlank()) {
            authenticate("auth-jwt") {
                this@authenticate.cardApiV1(
                    service = cardService,
                    contextConfig = contextConfig,
                )
                this@authenticate.dictionaryApiV1(
                    service = dictionaryService,
                    contextConfig = contextConfig,
                )
                this@authenticate.ttsApiV1(
                    service = ttsService,
                    contextConfig = contextConfig,
                )
                this@authenticate.translationApiV1(
                    service = translationService,
                    contextConfig = contextConfig,
                )
                this@authenticate.settingsApiV1(
                    service = settingsService,
                    contextConfig = contextConfig,
                )
            }
            authenticate("keycloakOAuth") {
                resource<Index> {
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
                            call.respond(thymeleafContent(tutorConfig, keycloakConfig, principal))
                        }
                    }
                }
            }
            health(
                service = healthService
            )
        } else {
            cardApiV1(
                service = cardService,
                contextConfig = contextConfig,
            )
            dictionaryApiV1(
                service = dictionaryService,
                contextConfig = contextConfig,
            )
            ttsApiV1(
                service = ttsService,
                contextConfig = contextConfig,
            )
            translationApiV1(
                service = translationService,
                contextConfig = contextConfig,
            )
            settingsApiV1(
                service = settingsService,
                contextConfig = contextConfig,
            )
            health(
                service = healthService
            )
            get("/") {
                call.respond(thymeleafContent(tutorConfig, keycloakConfig, null))
            }
        }
    }
}

internal fun makeJwtVerifier(jwkUrl: String, issuer: String): JWTVerifier =
    JWT.require(makeJwtAlgorithm(jwkUrl)).withIssuer(issuer).build()

internal fun makeJwtAlgorithm(jwkUrl: String): Algorithm {
    val jwkProvider =
        JwkProviderBuilder(URL(jwkUrl)).cached(/* cacheSize = */ 10,/* expiresIn = */ 24,/* unit = */ TimeUnit.HOURS
        ).rateLimited(/* bucketSize = */ 100,/* refillRate = */ 1,/* unit = */ TimeUnit.MINUTES
        ).build()

    val keyProvider = object : RSAKeyProvider {
        override fun getPublicKeyById(keyId: String?): RSAPublicKey = jwkProvider.get(keyId).publicKey as RSAPublicKey

        override fun getPrivateKey(): RSAPrivateKey? = null

        override fun getPrivateKeyId(): String? = null
    }

    return Algorithm.RSA256(keyProvider)
}

private fun thymeleafContent(
    tutorConfig: TutorConfig, keycloakConfig: KeycloakConfig, principal: OAuthAccessTokenResponse.OAuth2?
): ThymeleafContent {
    val res = mutableMapOf<String, Any>()
    val userConfig = if (principal == null) {
        mapOf(
            "user" to "dev",
            "devMode" to "true",
        )
    } else {
        mapOf(
            "user" to principal.name(),
            "keycloakAuthURL" to keycloakConfig.authorizeAddress,
            "keycloakAppRealm" to keycloakConfig.realm,
            "keycloakAppClient" to keycloakConfig.clientId,
        )
    }
    val commonConfig = mapOf(
        "numberOfRightAnswers" to tutorConfig.numberOfRightAnswers.toString(),
        "languages" to LANGUAGES,
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

@Resource("/")
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
    runConfig: RunConfig, keycloakConfig: KeycloakConfig, tutorConfig: TutorConfig
): String {
    val bootstrapServices = System.getProperty("BOOTSTRAP_SERVERS")
    val port = environment.config.property("ktor.deployment.port").getString()
    return """
            |
            |bootstrap-services             = $bootstrapServices
            |run-config-app-mode            = ${runConfig.mode}
            |run-config-debug-auth          = ${runConfig.auth}
            |keycloak-authorize-address     = ${keycloakConfig.authorizeAddress}
            |keycloak-access-token-address  = ${keycloakConfig.accessTokenAddress}
            |keycloak-redirect-address      = ${keycloakConfig.redirectAddress}
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