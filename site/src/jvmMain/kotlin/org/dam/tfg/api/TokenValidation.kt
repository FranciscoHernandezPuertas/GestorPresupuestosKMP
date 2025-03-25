package org.dam.tfg.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.TokenValidationRequest
import org.dam.tfg.util.JwtManager

@Api(routeOverride = "validatetoken")
suspend fun validateToken(context: ApiContext) {
    try {
        val request = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<TokenValidationRequest>(it)
        }

        if (request != null) {
            val user = JwtManager.verifyToken(request.token)
            if (user != null) {
                context.res.setBodyText(Json.encodeToString(user))
            } else {
                context.res.setBodyText(Json.encodeToString(ErrorResponse("Token inválido")))
            }
        } else {
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Falta el token")))
        }
    } catch (e: Exception) {
        context.res.setBodyText(Json.encodeToString(ErrorResponse(e.message ?: "Error desconocido")))
    }
}