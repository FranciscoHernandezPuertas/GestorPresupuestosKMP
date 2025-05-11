package org.dam.tfg.androidapp.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.MessageDigest
import java.util.*

object CryptoUtil {
    
    // SHA-256 hashing
    fun hashSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    // JWT encryption for formulas
    fun encryptFormula(formula: String, secret: String): String {
        val algorithm = Algorithm.HMAC256(secret)
        
        // First, encrypt the formula with SHA-256
        val encryptedFormula = hashSHA256(formula)
        
        // Then, create a JWT token with the encrypted formula
        return JWT.create()
            .withIssuer("formula-encryption")
            .withClaim("formula", encryptedFormula)
            .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
            .withIssuedAt(Date())
            .sign(algorithm)
    }
    
    // JWT decryption for formulas
    fun decryptFormula(jwtToken: String, secret: String): String? {
        return try {
            val algorithm = Algorithm.HMAC256(secret)
            val verifier = JWT.require(algorithm)
                .withIssuer("formula-encryption")
                .build()
            
            val decodedJWT = verifier.verify(jwtToken)
            decodedJWT.getClaim("formula").asString()
        } catch (e: Exception) {
            null
        }
    }
}
