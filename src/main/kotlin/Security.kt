package com.example

import com.example.auth.JwtConfig
import io.ktor.server.application.Application


import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install

import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM

            verifier(JwtConfig.verifier())

            validate { credential ->
                val accountId = credential.payload.getClaim("accountId").asInt()
                val email = credential.payload.getClaim("email").asString()
                val tenantCode = credential.payload.getClaim("tenantCode").asString()

                println("JWT DEBUG: accountId = $accountId")
                println("JWT DEBUG: email = $email")
                println("JWT DEBUG: tenantCode = $tenantCode")

                if (
                    accountId != null &&
                    !email.isNullOrBlank() &&
                    !tenantCode.isNullOrBlank()
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                println("JWT DEBUG: token rejected or expired")

                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or expired token")
                )
            }
        }
    }
}