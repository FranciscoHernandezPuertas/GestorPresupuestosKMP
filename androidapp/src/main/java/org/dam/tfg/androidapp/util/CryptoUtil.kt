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
        // Usamos FormulaEncryption en lugar de la implementación anterior
        return FormulaEncryption.encrypt(formula)
    }

    // JWT decryption for formulas
    fun decryptFormula(jwtToken: String, secret: String): String? {
        return try {
            // Usamos FormulaEncryption en lugar de la implementación anterior
            FormulaEncryption.decrypt(jwtToken)
        } catch (e: Exception) {
            null
        }
    }
}
