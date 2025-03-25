package org.dam.tfg.api

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

@Api(routeOverride = "usercheck")
suspend fun userCheck(context: ApiContext){
    try {
        val userRequest = context.req.body?.decodeToString()?.let { Json.decodeFromString<User>(it)}
        val user = userRequest?.let{
            context.data.getValue<MongoDB>().checkUserExistence(
                User(username = it.username, password = hashPassword(it.password))
            )
        }
        if(user != null){
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
                Json.encodeToString(
                    AuthResponse(
                        user = userWithoutPassword,
                        token = token
                    )
                )
            )
        } else {
            context.res.setBodyText(Json.encodeToString(ErrorResponse("El usuario no existe.")))
        }
    } catch (e: Exception) {
        context.res.setBodyText(Json.encodeToString(ErrorResponse(e.message ?: "Error desconocido")))
    }
}

@Api(routeOverride = "checkuserid")
suspend fun checkUserId(context: ApiContext) {
    try {
        val idRequest = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<String>(it)
        }
        val result = idRequest?.let {
            context.data.getValue<MongoDB>().checkUserId(it)
        }
        if(result != null) {
            context.res.setBodyText(Json.encodeToString(result))
        } else {
            context.res.setBodyText(Json.encodeToString(false))
        }
    } catch (e: Exception) {
        context.res.setBodyText(Json.encodeToString(false))
    }
}

private fun hashPassword(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hashBytes = messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
    val hexString = StringBuffer()

    for(byte in hashBytes) {
        hexString.append(String.format("%02x", byte))
    }
    return hexString.toString()
}