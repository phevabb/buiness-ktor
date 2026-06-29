package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.auth.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond

fun Application.configureSecurity() {

    install(Authentication) {

        // ✅ ✅ CLIENT AUTH (TENANT USERS)
        jwt("auth-jwt") {
            realm = JwtConfig.REALM

            verifier(JwtConfig.verifier())

            validate { credential ->
                val accountId = credential.payload.getClaim("accountId").asInt()
                val email = credential.payload.getClaim("email").asString()
                val tenantCode = credential.payload.getClaim("tenantCode").asString()

                if (
                    accountId != null &&
                    !email.isNullOrBlank() &&
                    !tenantCode.isNullOrBlank()
                ) {
                    JWTPrincipal(credential.payload)
                } else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or expired token")
                )
            }
        }

        // ✅ ✅ SUPER ADMIN AUTH (YOU 🔥)
        jwt("super-admin-jwt") {

            val secret = "super-secret-key"
            val issuer = "phena"

            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                val adminId = credential.payload.getClaim("adminId").asInt()

                if (adminId != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or expired super admin token")
                )
            }
        }
    }
}
