package com.example.superadmin.security



import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtService {

    private const val secret = "super-secret-key"
    private const val issuer = "phena"

    fun generateToken(adminId: Int): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("adminId", adminId)
            .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(secret))
    }
}