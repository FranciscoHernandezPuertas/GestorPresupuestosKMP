package org.dam.tfg.util

import com.varabyte.kobweb.browser.api
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.AuthResponse
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.TokenValidationRequest
import org.dam.tfg.models.User
import org.dam.tfg.models.UserWithoutPassword
import org.w3c.dom.get
import org.w3c.dom.set

suspend fun loginUser(user: User): UserWithoutPassword? {
    return try {
        val result = window.api.tryPost(
            apiPath = "usercheck",
            body = Json.encodeToString(user).encodeToByteArray()
        )

        result?.decodeToString()?.let { jsonResponse ->
            try {
                // Intentar parsear como respuesta exitosa
                val authResponse = Json.decodeFromString<AuthResponse>(jsonResponse)

                // Guardar datos en localStorage
                localStorage["jwt_token"] = authResponse.token
                localStorage["userType"] = authResponse.user.type
                localStorage["userId"] = authResponse.user.id
                localStorage["username"] = authResponse.user.username
                localStorage["remember"] = "true"

                authResponse.user
            } catch (e: Exception) {
                // Puede ser un error
                try {
                    val errorResponse = Json.decodeFromString<ErrorResponse>(jsonResponse)
                    console.error("Error de autenticación: ${errorResponse.message}")
                    null
                } catch (e: Exception) {
                    console.error("Error al parsear respuesta: $jsonResponse")
                    null
                }
            }
        }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

// Alias para mantener compatibilidad con código existente
suspend fun checkUserExistence(user: User): UserWithoutPassword? {
    return loginUser(user)
}

suspend fun validateToken(): UserWithoutPassword? {
    return try {
        val token = localStorage["jwt_token"] ?: return null

        val result = window.api.tryPost(
            apiPath = "validatetoken",
            body = Json.encodeToString(TokenValidationRequest(token)).encodeToByteArray()
        )

        result?.decodeToString()?.let { jsonResponse ->
            try {
                Json.decodeFromString<UserWithoutPassword>(jsonResponse)
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

suspend fun checkUserId(id: String): Boolean {
    return try {
        val result = window.api.tryPost(
            apiPath = "checkuserid",
            body = Json.encodeToString(id).encodeToByteArray()
        )

        result?.decodeToString()?.let { Json.decodeFromString<Boolean>(it) } ?: false
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

fun logout() {
    // Limpiar datos de autenticación
    localStorage.removeItem("jwt_token")
    localStorage.removeItem("userId")
    localStorage.removeItem("username")
    localStorage.removeItem("userType")
    localStorage["remember"] = "false"
}