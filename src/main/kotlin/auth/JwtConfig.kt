package com.example.auth



import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "CHANGE_THIS_SECRET_TO_SOMETHING_LONG"
    private const val issuer = "phena-api"
    private const val audience = "phena-client"

    private const val validityInMs = 24L * 60L * 60L * 1000L // 24 hours

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(
        accountId: Int,
        email: String,
        tenantCode: String
    ): String {
        val now = System.currentTimeMillis()

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("accountId", accountId)
            .withClaim("email", email)
            .withClaim("tenantCode", tenantCode)
            .withExpiresAt(Date(now + validityInMs))
            .sign(algorithm)
    }

    fun verifier() =
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
}