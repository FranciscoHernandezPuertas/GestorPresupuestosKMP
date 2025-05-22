package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.AuthResponse
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.User
import org.dam.tfg.models.UserWithoutPassword
import org.dam.tfg.util.JwtManager
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

// Configuración de Json para tolerancia
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Endpoint para la autenticación desde la app Android
 */
@Api(routeOverride = "android/auth/login")
suspend fun androidLogin(context: ApiContext) {
    try {
        // Parsear el cuerpo de la solicitud
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de usuario")

        val userRequest = try {
            json.decodeFromString<User>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de usuario: ${e.message}")
        }

        // Validar datos de entrada
        if (userRequest.username.isBlank() || userRequest.password.isBlank()) {
            throw Exception("Usuario y contraseña son requeridos")
        }

        // Aplicar hash a la contraseña
        val hashedPassword = hashPassword(userRequest.password)

        // Verificar credenciales
        val user = context.data.getValue<MongoDB>().checkUserExistence(
            User(username = userRequest.username, password = hashedPassword)
        )

        if (user != null) {
            // Crear objeto sin contraseña para el token
            val userWithoutPassword = UserWithoutPassword(
                id = user.id,
                username = user.username,
                type = user.type
            )

            // Generar token JWT
            val token = JwtManager.generateToken(userWithoutPassword)

            // Devolver respuesta con usuario y token
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse(
                        success = true,
                        data = AuthResponse(
                            user = userWithoutPassword,
                            token = token
                        )
                    )
                )
            )
        } else {
            // Credenciales inválidas
            context.res.status = 401 // Unauthorized
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<AuthResponse>(
                        success = false,
                        error = "Credenciales inválidas o usuario no existe"
                    )
                )
            )
        }
    } catch (e: Exception) {
        // Error en el servidor
        context.res.status = 500 // Internal Server Error
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<AuthResponse>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

// Función para crear un hash de la contraseña usando SHA-256
private fun hashPassword(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hashBytes = messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
    val hexString = StringBuilder()

    for (byte in hashBytes) {
        hexString.append(String.format("%02x", byte))
    }

    return hexString.toString()
}

