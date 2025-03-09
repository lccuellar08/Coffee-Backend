package com.lccuellar

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import io.ktor.server.resources.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.configurePlugins() {
    install(Resources)
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JWTConfig.REALM
            verifier(JWTConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("user_id").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.module() {

    DatabaseFactory.init()

    configurePlugins()
    configureRouting()
    configureHTTP()
}
