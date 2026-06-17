package com.example.auth



import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date





object JwtConfig {
    private const val SECRET = "CHANGE_THIS_SECRET_TO_SOMETHING_LONG"
    private const val ISSUER = "phena-api"
    private const val AUDIENCE = "phena-client"
    const val REALM = "phena-api"

    private const val VALIDITY_IN_MS = 24L * 60L * 60L * 1000L

    private val algorithm = Algorithm.HMAC256(SECRET)

    fun generateToken(
        accountId: Int,
        email: String,
        tenantCode: String
    ): String {
        val now = System.currentTimeMillis()

        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("accountId", accountId)
            .withClaim("email", email)
            .withClaim("tenantCode", tenantCode)
            .withExpiresAt(Date(now + VALIDITY_IN_MS))
            .sign(algorithm)
    }

    fun verifier() =
        JWT.require(algorithm)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .build()
}