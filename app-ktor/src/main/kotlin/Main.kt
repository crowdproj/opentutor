package com.gitlab.sszuev.flashcards

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.gitlab.sszuev.flashcards.api.apiV1
import com.gitlab.sszuev.flashcards.config.KeycloakConfig
import com.gitlab.sszuev.flashcards.config.RepositoriesConfig
import com.gitlab.sszuev.flashcards.logslib.logger
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.webjars.*
import kotlinx.html.*
import org.slf4j.event.Level
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.nio.charset.StandardCharsets
import java.util.*

// use to jetty, not netty, due to exception https://youtrack.jetbrains.com/issue/KTOR-4433
fun main(args: Array<String>) = io.ktor.server.jetty.EngineMain.main(args)

@KtorExperimentalLocationsAPI
@Suppress("unused")
fun Application.module(
    repositoriesConfig: RepositoriesConfig = RepositoriesConfig(),
    keycloakConfig: KeycloakConfig = KeycloakConfig(environment.config),
) {
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

    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(SerializationFeature.INDENT_OUTPUT)
            writerWithDefaultPrettyPrinter()
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(Locations)

    val service = cardService(repositoriesConfig.cardRepositories)
    val logger = logger(Route::class.java)

    routing {
        static("/static") {
            staticBasePackage = "static"
            resources(".")
        }
        authenticate("auth-jwt") {
            this@authenticate.apiV1(service, logger)
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
                        call.respond(ThymeleafContent("index", mapOf("user" to principal.name())))
                    }
                }
            }
        }
    }
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

private fun OAuthAccessTokenResponse.OAuth2?.name(): String {
    if (null == this) {
        return "Unauthorized"
    }
    val jwtToken = accessToken
    val token = JWT.decode(jwtToken)
    return token.getClaim("name").asString() ?: "Noname"
}