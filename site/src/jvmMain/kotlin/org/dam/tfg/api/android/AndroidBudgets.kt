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
import org.dam.tfg.models.table.Mesa
import org.bson.types.ObjectId

// Configuración de Json para tolerancia
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Obtener todos los presupuestos
 * CORREGIDO: Ruta simplificada, ya que Kobweb usa el package como prefijo
 */
@Api(routeOverride = "budgets")
suspend fun getAllAndroidBudgets(context: ApiContext) {
    try {
        val mesas = context.data.getValue<MongoDB>().getAllMesas()

        // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
        val presupuestosJsonArray = mesas.map { mesa ->
            mesaToJsonObject(mesa)
        }

        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to JsonArray(presupuestosJsonArray)
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<List<Mesa>>(
                    success = false,
                    error = "Error al obtener presupuestos: ${e.message}"
                )
            )
        )
    }
}

/**
 * Obtener presupuesto por ID
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "GET budgets/{id}")
suspend fun getAndroidBudgetById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de presupuesto no puede estar vacío")
        }

        val mesa = context.data.getValue<MongoDB>().getMesaById(id)
            ?: throw Exception("Presupuesto no encontrado")

        // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to mesaToJsonObject(mesa)
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
        val status = if (e.message?.contains("no encontrado") == true) 404 else 500
        context.res.status = status
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Mesa>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Crear un nuevo presupuesto
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "POST budgets")
suspend fun createAndroidBudget(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del presupuesto")

        // Primero parseamos como JsonObject para manejar id/_id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos principales, con preferencia por _id
        val mesaId = (jsonObject["_id"] ?: jsonObject["id"])?.jsonPrimitive?.content ?: ""

        // Extraer el resto de campos después
        val mesa = try {
            json.decodeFromString<Mesa>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos del presupuesto: ${e.message}")
        }

        // Validaciones
        if (!mesa.isValid()) {
            throw Exception(mesa.error)
        }

        // Crear presupuesto con ID generado si no tiene uno
        val newMesa = if (mesaId.isBlank() && mesa.id.isBlank()) {
            Mesa(
                id = ObjectId().toHexString(),
                tipo = mesa.tipo,
                tramos = mesa.tramos,
                elementosGenerales = mesa.elementosGenerales,
                cubetas = mesa.cubetas,
                modulos = mesa.modulos,
                precioTotal = mesa.precioTotal,
                fechaCreacion = mesa.fechaCreacion,
                username = mesa.username,
                error = mesa.error
            )
        } else if (mesaId.isNotBlank()) {
            // Si recibimos el ID del cliente, lo usamos (con preferencia por _id)
            Mesa(
                id = mesaId,
                tipo = mesa.tipo,
                tramos = mesa.tramos,
                elementosGenerales = mesa.elementosGenerales,
                cubetas = mesa.cubetas,
                modulos = mesa.modulos,
                precioTotal = mesa.precioTotal,
                fechaCreacion = mesa.fechaCreacion,
                username = mesa.username,
                error = mesa.error
            )
        } else {
            // Usar el id del objeto deserializado
            mesa
        }

        val success = context.data.getValue<MongoDB>().addMesa(newMesa)

        if (success) {
            context.res.status = 201 // Created

            // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to mesaToJsonObject(newMesa)
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo crear el presupuesto")
        }
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Mesa>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Actualizar un presupuesto existente
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "PUT budgets/{id}")
suspend fun updateAndroidBudget(context: ApiContext) {
    try {
        // Verificar que la petición es un PUT
        if (context.req.method.toString().lowercase() != "put") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de presupuesto no puede estar vacío")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del presupuesto")

        val mesa = try {
            json.decodeFromString<Mesa>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos del presupuesto: ${e.message}")
        }

        // Validaciones
        if (!mesa.isValid()) {
            throw Exception(mesa.error)
        }

        // Asegurar que el ID en el path coincida con el ID en el cuerpo
        val mesaToUpdate = Mesa(
            id = id,
            tipo = mesa.tipo,
            tramos = mesa.tramos,
            elementosGenerales = mesa.elementosGenerales,
            cubetas = mesa.cubetas,
            modulos = mesa.modulos,
            precioTotal = mesa.precioTotal,
            fechaCreacion = mesa.fechaCreacion,
            username = mesa.username,
            error = mesa.error
        )

        val success = context.data.getValue<MongoDB>().updateMesa(mesaToUpdate)

        if (success) {
            // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to mesaToJsonObject(mesaToUpdate)
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo actualizar el presupuesto. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Mesa>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Eliminar un presupuesto
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "budgets/delete/{id}")
suspend fun deleteAndroidBudget(context: ApiContext) {
    try {
        // Verificar que la petición es un DELETE
        if (context.req.method.toString().lowercase() != "delete") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de presupuesto no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteMesa(id)

        if (success) {
            // Crear respuesta adaptada para Android
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonPrimitive(true)
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo eliminar el presupuesto. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Boolean>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Función auxiliar para convertir un objeto Mesa en JsonObject con formato adecuado para Android
 */
private fun mesaToJsonObject(mesa: Mesa): JsonObject {
    // Convertir tramos a JSON
    val tramosJson = JsonArray(
        mesa.tramos.map { tramo ->
            JsonObject(mapOf(
                "numero" to JsonPrimitive(tramo.numero),
                "largo" to JsonPrimitive(tramo.largo),
                "ancho" to JsonPrimitive(tramo.ancho),
                "precio" to JsonPrimitive(tramo.precio),
                "tipo" to JsonPrimitive(tramo.tipo.toString()),
                "error" to JsonPrimitive(tramo.error)
            ))
        }
    )

    // Convertir elementos generales a JSON
    val elementosGeneralesJson = JsonArray(
        mesa.elementosGenerales.map { elemento ->
            // Crear el objeto límite
            val limiteJson = JsonObject(mapOf(
                "id" to JsonPrimitive(elemento.limite.id),
                "name" to JsonPrimitive(elemento.limite.name),
                "minQuantity" to JsonPrimitive(elemento.limite.minQuantity),
                "maxQuantity" to JsonPrimitive(elemento.limite.maxQuantity),
                "initialQuantity" to JsonPrimitive(elemento.limite.initialQuantity)
            ))

            JsonObject(mapOf(
                "nombre" to JsonPrimitive(elemento.nombre),
                "cantidad" to JsonPrimitive(elemento.cantidad),
                "precio" to JsonPrimitive(elemento.precio),
                "limite" to limiteJson
            ))
        }
    )

    // Convertir cubetas a JSON
    val cubetasJson = JsonArray(
        mesa.cubetas.map { cubeta ->
            JsonObject(mapOf(
                "tipo" to JsonPrimitive(cubeta.tipo),
                "numero" to JsonPrimitive(cubeta.numero),
                "largo" to JsonPrimitive(cubeta.largo),
                "fondo" to JsonPrimitive(cubeta.fondo),
                "alto" to JsonPrimitive(cubeta.alto),
                "precio" to JsonPrimitive(cubeta.precio),
                "error" to JsonPrimitive(cubeta.error),
                "minQuantity" to JsonPrimitive(cubeta.minQuantity)
            ))
        }
    )

    // Convertir módulos a JSON
    val modulosJson = JsonArray(
        mesa.modulos.map { modulo ->
            // Crear el objeto límite
            val limiteJson = JsonObject(mapOf(
                "id" to JsonPrimitive(modulo.limite.id),
                "name" to JsonPrimitive(modulo.limite.name),
                "minQuantity" to JsonPrimitive(modulo.limite.minQuantity),
                "maxQuantity" to JsonPrimitive(modulo.limite.maxQuantity),
                "initialQuantity" to JsonPrimitive(modulo.limite.initialQuantity)
            ))

            JsonObject(mapOf(
                "nombre" to JsonPrimitive(modulo.nombre),
                "largo" to JsonPrimitive(modulo.largo),
                "fondo" to JsonPrimitive(modulo.fondo),
                "alto" to JsonPrimitive(modulo.alto),
                "cantidad" to JsonPrimitive(modulo.cantidad),
                "limite" to limiteJson,
                "precio" to JsonPrimitive(modulo.precio)
            ))
        }
    )

    // Construir el objeto mesa completo
    return JsonObject(mapOf(
        "id" to JsonPrimitive(mesa.id), // Campo "id" para compatibilidad con Android
        "tipo" to JsonPrimitive(mesa.tipo),
        "tramos" to tramosJson,
        "elementosGenerales" to elementosGeneralesJson,
        "cubetas" to cubetasJson,
        "modulos" to modulosJson,
        "precioTotal" to JsonPrimitive(mesa.precioTotal),
        "fechaCreacion" to JsonPrimitive(mesa.fechaCreacion ?: ""),
        "username" to JsonPrimitive(mesa.username),
        "error" to JsonPrimitive(mesa.error)
    ))
}

