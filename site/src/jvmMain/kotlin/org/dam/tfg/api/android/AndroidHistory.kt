package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
 * CORREGIDO: Ruta simplificada, ya que Kobweb usa el package como prefijo
 */
@Api(routeOverride = "history")
suspend fun getAllAndroidHistory(context: ApiContext) {
    try {
        val history = context.data.getValue<MongoDB>().getAllHistory()

        // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
        val historyJsonArray = history.map { historyItem ->
            JsonObject(mapOf(
                "id" to JsonPrimitive(historyItem.id),
                "userId" to JsonPrimitive(historyItem.userId),
                "action" to JsonPrimitive(historyItem.action),
                "timestamp" to JsonPrimitive(historyItem.timestamp),
                "details" to JsonPrimitive(historyItem.details)
            ))
        }

        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to JsonArray(historyJsonArray)
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
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
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "GET history/{id}")
suspend fun getAndroidHistoryById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de historial no puede estar vacío")
        }

        val history = context.data.getValue<MongoDB>().getHistoryById(id)
            ?: throw Exception("Registro de historial no encontrado")

        // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to JsonObject(mapOf(
                "id" to JsonPrimitive(history.id),
                "userId" to JsonPrimitive(history.userId),
                "action" to JsonPrimitive(history.action),
                "timestamp" to JsonPrimitive(history.timestamp),
                "details" to JsonPrimitive(history.details)
            ))
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
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
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "POST history")
suspend fun createAndroidHistory(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del historial")

        // Primero parseamos como JsonObject para manejar id/_id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos, con preferencia por _id
        val historyId = (jsonObject["_id"] ?: jsonObject["id"])?.jsonPrimitive?.content ?: ""
        val userId = jsonObject["userId"]?.jsonPrimitive?.content ?: ""
        val action = jsonObject["action"]?.jsonPrimitive?.content ?: ""
        val timestamp = jsonObject["timestamp"]?.jsonPrimitive?.content ?: ""
        val details = jsonObject["details"]?.jsonPrimitive?.content ?: ""

        // Validaciones
        if (userId.isBlank()) {
            throw Exception("El ID de usuario no puede estar vacío")
        }

        if (action.isBlank()) {
            throw Exception("La acción no puede estar vacía")
        }

        if (timestamp.isBlank()) {
            throw Exception("La fecha/hora no puede estar vacía")
        }

        // Crear historial con ID generado si no tiene uno
        val newHistory = History(
            id = if (historyId.isBlank()) ObjectId().toHexString() else historyId,
            userId = userId,
            action = action,
            timestamp = timestamp,
            details = details
        )

        val success = context.data.getValue<MongoDB>().addHistory(newHistory)

        if (success) {
            context.res.status = 201 // Created

            // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "id" to JsonPrimitive(newHistory.id),
                    "userId" to JsonPrimitive(newHistory.userId),
                    "action" to JsonPrimitive(newHistory.action),
                    "timestamp" to JsonPrimitive(newHistory.timestamp),
                    "details" to JsonPrimitive(newHistory.details)
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
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
