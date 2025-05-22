package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
@Api(routeOverride = "auth/login")
suspend fun androidLogin(context: ApiContext) {
    try {
        println("=== DEBUG AUTH LOGIN ===")
        println("Method: ${context.req.method}")
        println("Path: ${context.req.connection}")
        println("Headers: ${context.req.headers}")

        // Parsear el cuerpo de la solicitud
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de usuario")

        println("Body received: $bodyText")

        // Primero parseamos como JsonObject para manejar _id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos, con preferencia por _id
        val userId = (jsonObject["_id"] ?: jsonObject["id"])?.jsonPrimitive?.content ?: ""
        val username = jsonObject["username"]?.jsonPrimitive?.content ?: ""
        val password = jsonObject["password"]?.jsonPrimitive?.content ?: ""
        val type = jsonObject["type"]?.jsonPrimitive?.content ?: "user"

        println("User parsed: username=$username, type=$type, id=$userId")

        // Validar datos de entrada
        if (username.isBlank() || password.isBlank()) {
            throw Exception("Usuario y contraseña son requeridos")
        }

        // Aplicar hash a la contraseña
        val hashedPassword = hashPassword(password)
        println("Password hashed successfully")

        // Verificar credenciales
        val user = context.data.getValue<MongoDB>().checkUserExistence(
            User(
                id = userId,
                username = username,
                password = hashedPassword,
                type = type
            )
        )

        println("User found: ${user != null}")
        if (user != null) {
            println("User details: id=${user.id}, username=${user.username}, type=${user.type}")
        }

        if (user != null) {
            // Crear objeto sin contraseña para el token
            val userWithoutPassword = UserWithoutPassword(
                id = user.id,
                username = user.username,
                type = user.type
            )

            // Generar token JWT
            val token = JwtManager.generateToken(userWithoutPassword)
            println("Token generated successfully")

            // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "user" to JsonObject(mapOf(
                        "id" to JsonPrimitive(userWithoutPassword.id),
                        "username" to JsonPrimitive(userWithoutPassword.username),
                        "type" to JsonPrimitive(userWithoutPassword.type)
                    )),
                    "token" to JsonPrimitive(token)
                ))
            ))

            val responseJson = json.encodeToString(responseJsonObject)
            println("Response to send: $responseJson")

            context.res.setBodyText(responseJson)
        } else {
            // Credenciales inválidas
            println("Invalid credentials")
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
        println("Exception in login: ${e.message}")
        e.printStackTrace()
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

@Api(routeOverride = "auth/test")
suspend fun testEndpoint(context: ApiContext) {
    try {
        // Verificar si podemos acceder a MongoDB
        val mongodb = context.data.getValue<MongoDB>()
        context.res.setBodyText("""{"success":true,"message":"Conexión correcta"}""")
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText("""{"success":false,"error":"${e.message}"}""")
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

