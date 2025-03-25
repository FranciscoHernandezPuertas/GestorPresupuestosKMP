package org.dam.tfg.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.dam.tfg.models.UserWithoutPassword
import java.util.Date

object JwtManager {
    private const val SECRET = "SUPERSECRET" // Cambiar por una clave segura
    private const val ISSUER = "tfg-app"
    private const val TOKEN_EXPIRATION = 86400000L // 24 horas

    private val algorithm = Algorithm.HMAC256(SECRET)

    fun generateToken(user: UserWithoutPassword): String {
        val now = Date()
        val expiresAt = Date(now.time + TOKEN_EXPIRATION)

        return JWT.create()
            .withIssuer(ISSUER)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .withSubject(user.id)
            .withClaim("username", user.username)
            .withClaim("type", user.type)
            .sign(algorithm)
    }

    fun verifyToken(token: String): UserWithoutPassword? {
        try {
            val verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build()

            val decoded = verifier.verify(token)

            return UserWithoutPassword(
                id = decoded.subject,
                username = decoded.getClaim("username").asString(),
                type = decoded.getClaim("type").asString()
            )
        } catch (e: JWTVerificationException) {
            return null
        }
    }
}