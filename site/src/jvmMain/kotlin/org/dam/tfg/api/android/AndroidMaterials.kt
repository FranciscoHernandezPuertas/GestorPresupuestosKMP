package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.Material
import org.bson.types.ObjectId

// Configuración de Json para tolerancia
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Obtener todos los materiales
 */
@Api(routeOverride = "GET /android/materials")
suspend fun getAllAndroidMaterials(context: ApiContext) {
    try {
        val materials = context.data.getValue<MongoDB>().getAllMaterials()

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = materials
                )
            )
        )
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<List<Material>>(
                    success = false,
                    error = "Error al obtener materiales: ${e.message}"
                )
            )
        )
    }
}

/**
 * Obtener material por ID
 */
@Api(routeOverride = "GET /android/materials/{id}")
suspend fun getAndroidMaterialById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val material = context.data.getValue<MongoDB>().getMaterialById(id)
            ?: throw Exception("Material no encontrado")

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = material
                )
            )
        )
    } catch (e: Exception) {
        val status = if (e.message?.contains("no encontrado") == true) 404 else 500
        context.res.status = status
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Material>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Crear un nuevo material
 */
@Api(routeOverride = "POST /android/materials")
suspend fun createAndroidMaterial(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del material")

        val material = try {
            json.decodeFromString<Material>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos del material: ${e.message}")
        }

        // Validaciones
        if (material.name.isBlank()) {
            throw Exception("El nombre del material no puede estar vacío")
        }

        if (material.price < 0) {
            throw Exception("El precio no puede ser negativo")
        }

        // Crear material con ID generado si no tiene uno
        val newMaterial = Material(
            id = if (material.id.isBlank()) ObjectId().toHexString() else material.id,
            name = material.name.trim(),
            price = material.price
        )

        val success = context.data.getValue<MongoDB>().addMaterial(newMaterial)

        if (success) {
            context.res.status = 201 // Created
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Material>(
                        success = true,
                        data = newMaterial
                    )
                )
            )
        } else {
            throw Exception("No se pudo crear el material")
        }
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Material>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Actualizar un material existente
 */
@Api(routeOverride = "PUT /android/materials/{id}")
suspend fun updateAndroidMaterial(context: ApiContext) {
    try {
        // Verificar que la petición es un PUT
        if (context.req.method.toString().lowercase() != "put") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del material")

        val material = try {
            json.decodeFromString<Material>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos del material: ${e.message}")
        }

        // Validaciones
        if (material.name.isBlank()) {
            throw Exception("El nombre del material no puede estar vacío")
        }

        if (material.price < 0) {
            throw Exception("El precio no puede ser negativo")
        }

        // Asegurar que el ID en el path coincida con el ID en el cuerpo
        val materialToUpdate = Material(
            id = id,
            name = material.name,
            price = material.price
        )

        val success = context.data.getValue<MongoDB>().updateMaterial(materialToUpdate)

        if (success) {
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Material>(
                        success = true,
                        data = materialToUpdate
                    )
                )
            )
        } else {
            throw Exception("No se pudo actualizar el material. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Material>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Eliminar un material
 */
@Api(routeOverride = "DELETE /android/materials/{id}")
suspend fun deleteAndroidMaterial(context: ApiContext) {
    try {
        // Verificar que la petición es un DELETE
        if (context.req.method.toString().lowercase() != "delete") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteMaterial(id)

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
            throw Exception("No se pudo eliminar el material. Posiblemente no existe.")
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
