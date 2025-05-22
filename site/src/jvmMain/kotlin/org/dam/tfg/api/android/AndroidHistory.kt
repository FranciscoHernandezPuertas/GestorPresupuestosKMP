package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.History
import org.bson.types.ObjectId

// Configuración de Json para tolerancia
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Obtener todo el historial
 */
@Api(routeOverride = "android/history")
suspend fun getAllAndroidHistory(context: ApiContext) {
    try {
        val history = context.data.getValue<MongoDB>().getAllHistory()

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = history
                )
            )
        )
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<List<History>>(
                    success = false,
                    error = "Error al obtener historial: ${e.message}"
                )
            )
        )
    }
}

/**
 * Obtener registro de historial por ID
 */
@Api(routeOverride = "android/history/{id}")
suspend fun getAndroidHistoryById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de historial no puede estar vacío")
        }

        val history = context.data.getValue<MongoDB>().getHistoryById(id)
            ?: throw Exception("Registro de historial no encontrado")

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = history
                )
            )
        )
    } catch (e: Exception) {
        val status = if (e.message?.contains("no encontrado") == true) 404 else 500
        context.res.status = status
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<History>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Crear un nuevo registro de historial
 */
@Api(routeOverride = "android/history")
suspend fun createAndroidHistory(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del historial")

        val history = try {
            json.decodeFromString<History>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos del historial: ${e.message}")
        }

        // Validaciones
        if (history.userId.isBlank()) {
            throw Exception("El ID de usuario no puede estar vacío")
        }

        if (history.action.isBlank()) {
            throw Exception("La acción no puede estar vacía")
        }

        if (history.timestamp.isBlank()) {
            throw Exception("La fecha/hora no puede estar vacía")
        }

        // Crear historial con ID generado si no tiene uno
        val newHistory = if (history.id.isBlank()) {
            History(
                id = ObjectId().toHexString(),
                userId = history.userId,
                action = history.action,
                timestamp = history.timestamp,
                details = history.details
            )
        } else {
            history
        }

        val success = context.data.getValue<MongoDB>().addHistory(newHistory)

        if (success) {
            context.res.status = 201 // Created
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<History>(
                        success = true,
                        data = newHistory
                    )
                )
            )
        } else {
            throw Exception("No se pudo crear el registro de historial")
        }
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<History>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}
