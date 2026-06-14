package plugins



import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureAuth() {
    val secret = environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: "super-secret"

    val audience = environment.config.propertyOrNull("jwt.audience")?.getString()
        ?: "ktor-users"

    val issuer = environment.config.propertyOrNull("jwt.issuer")?.getString()
        ?: "ktor-api"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor-business"

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                val accountId = credential.payload.getClaim("accountId").asInt()
                    ?: credential.payload.getClaim("userId").asInt()

                if (accountId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}