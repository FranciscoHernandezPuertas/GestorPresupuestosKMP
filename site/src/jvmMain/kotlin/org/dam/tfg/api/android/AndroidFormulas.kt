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
import org.dam.tfg.models.Formula
import org.dam.tfg.util.FormulaEncryption
import org.bson.types.ObjectId

// Configuración de Json para tolerancia
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Obtener todas las fórmulas
 */
@Api(routeOverride = "formulas/list")
suspend fun getAllAndroidFormulas(context: ApiContext) {
    try {
        println("=== GET ALL FORMULAS ===")
        // Obtener el tipo de usuario para determinar si puede ver fórmulas desencriptadas
        val userType = context.req.headers["X-User-Type"] ?: "user"
        println("User type: $userType")

        val formulas = context.data.getValue<MongoDB>().getAllFormulas()
        println("Found ${formulas.size} formulas")

        // Si el usuario es admin, desencriptar las fórmulas
        val resultFormulas = if (FormulaEncryption.canViewFormula(userType.toString())) {
            formulas.map { formula ->
                try {
                    if (formula.formulaEncrypted) {
                        Formula(
                            id = formula.id,
                            name = formula.name,
                            formula = FormulaEncryption.decrypt(formula.formula),
                            formulaEncrypted = false,
                            variables = formula.variables
                        )
                    } else {
                        formula
                    }
                } catch (e: Exception) {
                    println("Error decrypting formula ${formula.id}: ${e.message}")
                    // Si hay error al desencriptar una fórmula, la dejamos como está
                    formula
                }
            }
        } else {
            formulas
        }

        // Crear respuesta adaptada para Android (con ambos campos "id" y "_id" para compatibilidad)
        val formulasJsonArray = resultFormulas.map { formula ->
            val variablesMap = formula.variables.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap()

            JsonObject(mapOf(
                "_id" to JsonPrimitive(formula.id),
                "id" to JsonPrimitive(formula.id),
                "name" to JsonPrimitive(formula.name),
                "formula" to JsonPrimitive(formula.formula),
                "formulaEncrypted" to JsonPrimitive(formula.formulaEncrypted),
                "variables" to JsonObject(variablesMap)
            ))
        }

        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to kotlinx.serialization.json.JsonArray(formulasJsonArray)
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
        println("Error getting all formulas: ${e.message}")
        e.printStackTrace()
        context.res.status = 500
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<List<Formula>>(
                    success = false,
                    error = "Error al obtener fórmulas: ${e.message}"
                )
            )
        )
    }
}

/**
 * Obtener fórmula por ID
 */
@Api(routeOverride = "formulas/get/{id}")
suspend fun getAndroidFormulaById(context: ApiContext) {
    // Verificar que la petición es un GET
    if (context.req.method.toString().lowercase() != "get") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Formula>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("=== GET FORMULA BY ID ===")
        println("Requested ID: $id")

        // Obtener el tipo de usuario para determinar si puede ver fórmulas desencriptadas
        val userType = context.req.headers["X-User-Type"] ?: "user"
        println("User type: $userType")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val formula = context.data.getValue<MongoDB>().getFormulaById(id)
            ?: throw Exception("Fórmula no encontrada")

        println("Formula found: ${formula.name} with ID: ${formula.id}")

        // Si el usuario es admin, desencriptar la fórmula
        val resultFormula = if (FormulaEncryption.canViewFormula(userType.toString()) && formula.formulaEncrypted) {
            try {
                Formula(
                    id = formula.id,
                    name = formula.name,
                    formula = FormulaEncryption.decrypt(formula.formula),
                    formulaEncrypted = false,
                    variables = formula.variables
                )
            } catch (e: Exception) {
                println("Error decrypting formula: ${e.message}")
                // Si hay error al desencriptar, la dejamos como está
                formula
            }
        } else {
            formula
        }

        // Crear respuesta adaptada para Android (con ambos campos "id" y "_id" para compatibilidad)
        val variablesMap = resultFormula.variables.map { entry ->
            entry.key to JsonPrimitive(entry.value)
        }.toMap()

        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to JsonObject(mapOf(
                "_id" to JsonPrimitive(resultFormula.id),
                "id" to JsonPrimitive(resultFormula.id),
                "name" to JsonPrimitive(resultFormula.name),
                "formula" to JsonPrimitive(resultFormula.formula),
                "formulaEncrypted" to JsonPrimitive(resultFormula.formulaEncrypted),
                "variables" to JsonObject(variablesMap)
            ))
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
        println("Error getting formula by ID: ${e.message}")
        e.printStackTrace()
        val status = if (e.message?.contains("no encontrada") == true) 404 else 500
        context.res.status = status
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Formula>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Crear una nueva fórmula
 */
@Api(routeOverride = "formulas/create")
suspend fun createAndroidFormula(context: ApiContext) {
    // Verificar que la petición es un POST
    if (context.req.method.toString().lowercase() != "post") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Formula>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        println("=== CREATE FORMULA ===")

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de la fórmula")

        println("Body received: $bodyText")

        // Primero parseamos como JsonObject para manejar id/_id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos, con preferencia por _id
        val id = (jsonObject["_id"] ?: jsonObject["id"])?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            ?: ObjectId().toHexString()
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        val formulaText = jsonObject["formula"]?.jsonPrimitive?.content ?: ""

        // Extraer variables
        val variablesMap = mutableMapOf<String, String>()
        jsonObject["variables"]?.let { variablesElement ->
            if (variablesElement is JsonObject) {
                variablesElement.entries.forEach { (key, value) ->
                    if (value is JsonPrimitive && value.isString) {
                        variablesMap[key] = value.content
                    }
                }
            }
        }

        // Validaciones
        if (name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formulaText.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        // Obtener el tipo de usuario para determinar si necesitamos encriptar
        val userType = context.req.headers["X-User-Type"] ?: "user"
        println("User type for formula creation: $userType")

        // Siempre encriptar la fórmula al guardarla
        val encryptedFormula = FormulaEncryption.encrypt(formulaText)
        println("Formula encrypted successfully")

        // Crear la fórmula con la fórmula encriptada
        val newFormula = Formula(
            id = id,
            name = name,
            formula = encryptedFormula,
            formulaEncrypted = true, // Siempre true porque encriptamos
            variables = variablesMap
        )

        val success = context.data.getValue<MongoDB>().addFormula(newFormula)

        if (success) {
            println("Formula created successfully with ID: ${newFormula.id}")
            context.res.status = 201 // Created

            // Si el usuario puede ver fórmulas desencriptadas, devolvemos la versión desencriptada
            val resultFormula = if (FormulaEncryption.canViewFormula(userType.toString())) {
                Formula(
                    id = newFormula.id,
                    name = newFormula.name,
                    formula = formulaText, // La fórmula original sin encriptar
                    formulaEncrypted = false,
                    variables = newFormula.variables
                )
            } else {
                newFormula
            }

            // Crear respuesta adaptada para Android
            val variablesJsonObject = JsonObject(resultFormula.variables.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap())

            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "_id" to JsonPrimitive(resultFormula.id),
                    "id" to JsonPrimitive(resultFormula.id),
                    "name" to JsonPrimitive(resultFormula.name),
                    "formula" to JsonPrimitive(resultFormula.formula),
                    "formulaEncrypted" to JsonPrimitive(resultFormula.formulaEncrypted),
                    "variables" to variablesJsonObject
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo crear la fórmula")
        }
    } catch (e: Exception) {
        println("Error creating formula: ${e.message}")
        e.printStackTrace()
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Formula>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Actualizar una fórmula existente
 */
@Api(routeOverride = "formulas/update/{id}")
suspend fun updateAndroidFormula(context: ApiContext) {
    // Verificar que la petición es un PUT
    if (context.req.method.toString().lowercase() != "put") {
        context.res.status = 405 // Method Not Allowed
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Formula>(
                    success = false,
                    error = "Método no permitido"
                )
            )
        )
        return
    }

    try {
        println("=== UPDATE FORMULA ===")

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Updating formula ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        // Verificar que la fórmula existe
        val existingFormula = context.data.getValue<MongoDB>().getFormulaById(id)
            ?: throw Exception("Fórmula no encontrada con el ID: $id")

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de la fórmula")

        println("Body received: $bodyText")

        // Parsear el cuerpo como JsonObject
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        val formulaText = jsonObject["formula"]?.jsonPrimitive?.content ?: ""

        // Extraer variables
        val variablesMap = mutableMapOf<String, String>()
        jsonObject["variables"]?.let { variablesElement ->
            if (variablesElement is JsonObject) {
                variablesElement.entries.forEach { (key, value) ->
                    if (value is JsonPrimitive && value.isString) {
                        variablesMap[key] = value.content
                    }
                }
            }
        }

        // Validaciones
        if (name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formulaText.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        // Obtener el tipo de usuario para determinar si puede ver fórmulas desencriptadas
        val userType = context.req.headers["X-User-Type"] ?: "user"
        println("User type for formula update: $userType")

        // Siempre encriptar la fórmula al guardarla
        val encryptedFormula = FormulaEncryption.encrypt(formulaText)
        println("Formula encrypted successfully for update")

        // Crear la fórmula actualizada con la fórmula encriptada
        val updatedFormula = Formula(
            id = id,
            name = name,
            formula = encryptedFormula,
            formulaEncrypted = true, // Siempre true porque encriptamos
            variables = variablesMap
        )

        val success = context.data.getValue<MongoDB>().updateFormula(updatedFormula)

        if (success) {
            println("Formula updated successfully: ${updatedFormula.id}")

            // Si el usuario puede ver fórmulas desencriptadas, devolvemos la versión desencriptada
            val resultFormula = if (FormulaEncryption.canViewFormula(userType.toString())) {
                Formula(
                    id = updatedFormula.id,
                    name = updatedFormula.name,
                    formula = formulaText, // La fórmula original sin encriptar
                    formulaEncrypted = false,
                    variables = updatedFormula.variables
                )
            } else {
                updatedFormula
            }

            // Crear respuesta adaptada para Android
            val variablesJsonObject = JsonObject(resultFormula.variables.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap())

            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "_id" to JsonPrimitive(resultFormula.id),
                    "id" to JsonPrimitive(resultFormula.id),
                    "name" to JsonPrimitive(resultFormula.name),
                    "formula" to JsonPrimitive(resultFormula.formula),
                    "formulaEncrypted" to JsonPrimitive(resultFormula.formulaEncrypted),
                    "variables" to variablesJsonObject
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo actualizar la fórmula")
        }
    } catch (e: Exception) {
        println("Error updating formula: ${e.message}")
        e.printStackTrace()
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Formula>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Eliminar una fórmula
 */
@Api(routeOverride = "formulas/delete/{id}")
suspend fun deleteAndroidFormula(context: ApiContext) {
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
        println("=== DELETE FORMULA ===")

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Deleting formula ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteFormula(id)

        if (success) {
            println("Formula deleted successfully: $id")

            // Crear respuesta adaptada para Android
            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonPrimitive(true)
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo eliminar la fórmula. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        println("Error deleting formula: ${e.message}")
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

