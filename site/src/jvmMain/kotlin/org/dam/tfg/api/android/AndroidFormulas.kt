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

        // ...existing code...
    } catch (e: Exception) {
        // ...existing code...
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

        // ...existing code...
    } catch (e: Exception) {
        // ...existing code...
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

        // ...existing code...
    } catch (e: Exception) {
        // ...existing code...
    }
}

