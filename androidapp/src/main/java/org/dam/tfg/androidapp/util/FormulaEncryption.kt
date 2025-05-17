package org.dam.tfg.androidapp.util

import android.util.Base64
import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Utilidad para encriptar y desencriptar fórmulas usando AES + JWT
 */
object FormulaEncryption {
    private const val TAG = "FormulaEncryption"
    private const val SECRET = "SUPERSECRET12345" // Clave para AES
    private const val ISSUER = "formula-encryption"
    private val algorithm = Algorithm.HMAC256(SECRET)
    private const val EXPIRATION_TIME = 7_776_000_000L // 90 días en milisegundos

    // Encripta una fórmula usando AES + JWT
    fun encrypt(plainFormula: String): String {
        try {
            // Primero encriptamos con AES
            val encryptedAES = encryptAES(plainFormula)

            // Luego envolvemos en un JWT
            return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("formula", encryptedAES)
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm)
        } catch (e: Exception) {
            Log.e(TAG, "Error al encriptar fórmula: ${e.message}", e)
            throw e
        }
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
            Log.e(TAG, "La fórmula está dañada o ha expirado: ${e.message}", e)
            throw Exception("La fórmula está dañada o ha expirado: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al desencriptar fórmula: ${e.message}", e)
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
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    // Desencriptación AES
    private fun decryptAES(encryptedData: String): String {
        val key = SecretKeySpec(SECRET.substring(0, 16).toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
}
