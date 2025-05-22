package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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
 */
@Api(routeOverride = "budgets")
suspend fun getAllAndroidBudgets(context: ApiContext) {
    try {
        val mesas = context.data.getValue<MongoDB>().getAllMesas()

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = mesas
                )
            )
        )
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
 */
@Api(routeOverride = "GET /android/budgets/{id}")
suspend fun getAndroidBudgetById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de presupuesto no puede estar vacío")
        }

        val mesa = context.data.getValue<MongoDB>().getMesaById(id)
            ?: throw Exception("Presupuesto no encontrado")

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = mesa
                )
            )
        )
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
 */
@Api(routeOverride = "POST /android/budgets")
suspend fun createAndroidBudget(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
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

        // Crear presupuesto con ID generado si no tiene uno
        val newMesa = if (mesa.id.isBlank()) {
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
        } else {
            mesa
        }

        val success = context.data.getValue<MongoDB>().addMesa(newMesa)

        if (success) {
            context.res.status = 201 // Created
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Mesa>(
                        success = true,
                        data = newMesa
                    )
                )
            )
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
 */
@Api(routeOverride = "PUT /android/budgets/{id}")
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
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Mesa>(
                        success = true,
                        data = mesaToUpdate
                    )
                )
            )
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
 */
@Api(routeOverride = "DELETE /android/budgets/{id}")
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
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Boolean>(
                        success = true,
                        data = true
                    )
                )
            )
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
