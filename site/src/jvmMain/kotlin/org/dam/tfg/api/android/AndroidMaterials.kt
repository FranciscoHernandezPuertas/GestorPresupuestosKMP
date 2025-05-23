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
@Api(routeOverride = "materials/list")
suspend fun getAllAndroidMaterials(context: ApiContext) {
    try {
        println("=== GET ALL MATERIALS ===")
        val materials = context.data.getValue<MongoDB>().getAllMaterials()
        println("Found ${materials.size} materials")

        // Crear respuesta adaptada para Android (con ambos campos "id" y "_id" para compatibilidad)
        val materialsJsonArray = materials.map { material ->
            JsonObject(mapOf(
                "_id" to JsonPrimitive(material.id),
                "id" to JsonPrimitive(material.id),
                "name" to JsonPrimitive(material.name),
                "price" to JsonPrimitive(material.price)
            ))
        }

        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to kotlinx.serialization.json.JsonArray(materialsJsonArray)
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
        println("Error getting all materials: ${e.message}")
        e.printStackTrace()
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
@Api(routeOverride = "materials/get/{id}")
suspend fun getAndroidMaterialById(context: ApiContext) {
    // Verificar que la petición es un GET
    if (context.req.method.toString().lowercase() != "get") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Material>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("=== GET MATERIAL BY ID ===")
        println("Requested ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val material = context.data.getValue<MongoDB>().getMaterialById(id)
            ?: throw Exception("Material no encontrado")

        println("Material found: ${material.name} with ID: ${material.id}")

        // Crear respuesta adaptada para Android (con ambos campos "id" y "_id" para compatibilidad)
        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to JsonObject(mapOf(
                "_id" to JsonPrimitive(material.id),
                "id" to JsonPrimitive(material.id),
                "name" to JsonPrimitive(material.name),
                "price" to JsonPrimitive(material.price)
            ))
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
        println("Error getting material by ID: ${e.message}")
        e.printStackTrace()
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
@Api(routeOverride = "materials/create")
suspend fun createAndroidMaterial(context: ApiContext) {
    // Verificar que la petición es un POST
    if (context.req.method.toString().lowercase() != "post") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Material>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        println("=== CREATE MATERIAL ===")

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del material")

        println("Body received: $bodyText")

        // Primero parseamos como JsonObject para manejar id/_id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos, con preferencia por _id
        val materialId = (jsonObject["_id"] ?: jsonObject["id"])?.jsonPrimitive?.content ?: ""
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        val price = jsonObject["price"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

        // Validaciones
        if (name.isBlank()) {
            throw Exception("El nombre del material no puede estar vacío")
        }

        if (price < 0) {
            throw Exception("El precio no puede ser negativo")
        }

        // Crear material con ID generado si no tiene uno
        val newMaterial = Material(
            id = if (materialId.isBlank()) ObjectId().toHexString() else materialId,
            name = name.trim(),
            price = price
        )

        println("Creating material with ID: ${newMaterial.id}")
        val success = context.data.getValue<MongoDB>().addMaterial(newMaterial)

        if (success) {
            println("Material created successfully")
            context.res.status = 201 // Created

            // Crear respuesta adaptada para Android (con ambos campos "id" y "_id" para compatibilidad)
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "_id" to JsonPrimitive(newMaterial.id),
                    "id" to JsonPrimitive(newMaterial.id),
                    "name" to JsonPrimitive(newMaterial.name),
                    "price" to JsonPrimitive(newMaterial.price)
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo crear el material")
        }
    } catch (e: Exception) {
        println("Error creating material: ${e.message}")
        e.printStackTrace()
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
@Api(routeOverride = "materials/update/{id}")
suspend fun updateAndroidMaterial(context: ApiContext) {
    // Verificar que la petición es un PUT
    if (context.req.method.toString().lowercase() != "put") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Material>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        println("=== UPDATE MATERIAL ===")

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Updating material ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos del material")

        println("Body received: $bodyText")

        // Primero parseamos como JsonObject para manejar id/_id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        val price = jsonObject["price"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

        // Validaciones
        if (name.isBlank()) {
            throw Exception("El nombre del material no puede estar vacío")
        }

        if (price < 0) {
            throw Exception("El precio no puede ser negativo")
        }

        // Asegurar que el ID en el path coincida con el ID en el cuerpo
        val materialToUpdate = Material(
            id = id,
            name = name,
            price = price
        )

        println("Updating material: name=${materialToUpdate.name}, price=${materialToUpdate.price}")
        val success = context.data.getValue<MongoDB>().updateMaterial(materialToUpdate)

        if (success) {
            println("Material updated successfully")
            // Crear respuesta adaptada para Android (con ambos campos "id" y "_id" para compatibilidad)
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "_id" to JsonPrimitive(materialToUpdate.id),
                    "id" to JsonPrimitive(materialToUpdate.id),
                    "name" to JsonPrimitive(materialToUpdate.name),
                    "price" to JsonPrimitive(materialToUpdate.price)
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo actualizar el material. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        println("Error updating material: ${e.message}")
        e.printStackTrace()
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
@Api(routeOverride = "materials/delete/{id}")
suspend fun deleteAndroidMaterial(context: ApiContext) {
    // Verificar que la petición es un DELETE
    if (context.req.method.toString().lowercase() != "delete") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Boolean>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        println("=== DELETE MATERIAL ===")

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Deleting material ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteMaterial(id)

        if (success) {
            println("Material deleted successfully")
            // Crear respuesta adaptada para Android
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonPrimitive(true)
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo eliminar el material. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        println("Error deleting material: ${e.message}")
        e.printStackTrace()
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
