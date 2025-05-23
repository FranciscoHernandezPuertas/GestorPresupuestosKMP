package org.dam.tfg.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Base64
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object FormulaEncryption {
    // Actualizado para que coincida con la app Android
    private val SECRET = System.getenv("SECRET") ?: run {
        System.err.println("SECRET no encontrado, usando URI por defecto")
        "SECRET12345"
    }
    private const val ISSUER = "formula-encryption"
    private val algorithm = Algorithm.HMAC256(SECRET)
    private const val EXPIRATION_TIME = 7_776_000_000L // 90 días en milisegundos

    // Encripta una fórmula usando AES + JWT
    fun encrypt(plainFormula: String): String {
        // Primero encriptamos con AES
        val encryptedAES = encryptAES(plainFormula)

        // Luego envolvemos en un JWT
        return JWT.create()
            .withIssuer(ISSUER)
            .withClaim("formula", encryptedAES)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(algorithm)
    }

    // Desencripta una fórmula JWT+AES
    fun decrypt(encryptedJwt: String): String {
        try {
            val verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build()

            val decoded = verifier.verify(encryptedJwt)
            val encryptedAES = decoded.getClaim("formula").asString()

            return decryptAES(encryptedAES)
        } catch (e: JWTVerificationException) {
            throw Exception("La fórmula está dañada o ha expirado: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error al desencriptar fórmula: ${e.message}")
        }
    }

    // Determina si un tipo de usuario puede ver la fórmula sin encriptar
    fun canViewFormula(userType: String): Boolean {
        return userType == "admin"
    }

    // Encriptación AES
    private fun encryptAES(data: String): String {
        val key = SecretKeySpec(SECRET.substring(0, 16).toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // Desencriptación AES
    private fun decryptAES(encryptedData: String): String {
        val key = SecretKeySpec(SECRET.substring(0, 16).toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.getDecoder().decode(encryptedData)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
}

