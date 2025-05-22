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
 * CORREGIDO: Ruta simplificada, ya que Kobweb usa el package como prefijo
 */
@Api(routeOverride = "formulas")
suspend fun getAllAndroidFormulas(context: ApiContext) {
    try {
        // Obtener el tipo de usuario para determinar si puede ver fórmulas desencriptadas
        val userType = context.req.headers["X-User-Type"] ?: "user"

        val formulas = context.data.getValue<MongoDB>().getAllFormulas()

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
                    // Si hay error al desencriptar una fórmula, la dejamos como está
                    formula
                }
            }
        } else {
            formulas
        }

        // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
        val formulasJsonArray = resultFormulas.map { formula ->
            val variablesMap = formula.variables.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap()

            JsonObject(mapOf(
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
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "GET formulas/{id}")
suspend fun getAndroidFormulaById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        // Obtener el tipo de usuario para determinar si puede ver fórmulas desencriptadas
        val userType = context.req.headers["X-User-Type"] ?: "user"

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val formula = context.data.getValue<MongoDB>().getFormulaById(id)
            ?: throw Exception("Fórmula no encontrada")

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
                // Si hay error al desencriptar, la dejamos como está
                formula
            }
        } else {
            formula
        }

        // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
        val variablesMap = resultFormula.variables.map { entry ->
            entry.key to JsonPrimitive(entry.value)
        }.toMap()

        val responseJsonObject = JsonObject(mapOf(
            "success" to JsonPrimitive(true),
            "data" to JsonObject(mapOf(
                "id" to JsonPrimitive(resultFormula.id),
                "name" to JsonPrimitive(resultFormula.name),
                "formula" to JsonPrimitive(resultFormula.formula),
                "formulaEncrypted" to JsonPrimitive(resultFormula.formulaEncrypted),
                "variables" to JsonObject(variablesMap)
            ))
        ))

        context.res.setBodyText(json.encodeToString(responseJsonObject))
    } catch (e: Exception) {
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
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "POST formulas")
suspend fun createAndroidFormula(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de la fórmula")

        // Primero parseamos como JsonObject para manejar id/_id correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos, con preferencia por _id
        val formulaId = (jsonObject["_id"] ?: jsonObject["id"])?.jsonPrimitive?.content ?: ""
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        val formulaText = jsonObject["formula"]?.jsonPrimitive?.content ?: ""
        val formulaEncrypted = jsonObject["formulaEncrypted"]?.jsonPrimitive?.content?.toBoolean() ?: false

        // Extraer variables (que es un objeto anidado)
        val variablesObject = jsonObject["variables"]?.jsonObject
        val variables = variablesObject?.let { obj ->
            obj.entries.associate { entry ->
                entry.key to (entry.value.jsonPrimitive.content)
            }
        } ?: emptyMap()

        // Validaciones básicas
        if (name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formulaText.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        // Encriptar la fórmula si no está encriptada
        val encryptedFormula = if (!formulaEncrypted) {
            FormulaEncryption.encrypt(formulaText)
        } else {
            formulaText
        }

        // Crear fórmula con ID generado si no tiene uno
        val newFormula = Formula(
            id = if (formulaId.isBlank()) ObjectId().toHexString() else formulaId,
            name = name.trim(),
            formula = encryptedFormula,
            formulaEncrypted = true, // Siempre marcamos como encriptada
            variables = variables
        )

        val success = context.data.getValue<MongoDB>().addFormula(newFormula)

        if (success) {
            context.res.status = 201 // Created

            // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
            val variablesMap = newFormula.variables.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap()

            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "id" to JsonPrimitive(newFormula.id),
                    "name" to JsonPrimitive(newFormula.name),
                    "formula" to JsonPrimitive(newFormula.formula),
                    "formulaEncrypted" to JsonPrimitive(newFormula.formulaEncrypted),
                    "variables" to JsonObject(variablesMap)
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo crear la fórmula")
        }
    } catch (e: Exception) {
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
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "PUT formulas/{id}")
suspend fun updateAndroidFormula(context: ApiContext) {
    try {
        // Verificar que la petición es un PUT
        if (context.req.method.toString().lowercase() != "put") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de la fórmula")

        // Primero parseamos como JsonObject para manejar correctamente
        val jsonElement = json.parseToJsonElement(bodyText)
        val jsonObject = jsonElement.jsonObject

        // Extraer campos
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        val formulaText = jsonObject["formula"]?.jsonPrimitive?.content ?: ""
        val formulaEncrypted = jsonObject["formulaEncrypted"]?.jsonPrimitive?.content?.toBoolean() ?: false

        // Extraer variables (que es un objeto anidado)
        val variablesObject = jsonObject["variables"]?.jsonObject
        val variables = variablesObject?.let { obj ->
            obj.entries.associate { entry ->
                entry.key to (entry.value.jsonPrimitive.content)
            }
        } ?: emptyMap()

        // Validaciones
        if (name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formulaText.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        // Encriptar la fórmula si no está encriptada
        val encryptedFormula = if (!formulaEncrypted) {
            FormulaEncryption.encrypt(formulaText)
        } else {
            formulaText
        }

        // Asegurar que el ID en el path coincida con el ID en el cuerpo
        val formulaToUpdate = Formula(
            id = id,
            name = name.trim(),
            formula = encryptedFormula,
            formulaEncrypted = true, // Siempre marcamos como encriptada
            variables = variables
        )

        val success = context.data.getValue<MongoDB>().updateFormula(formulaToUpdate)

        if (success) {
            // Crear respuesta adaptada para Android (con el campo "id" en lugar de "_id")
            val variablesMap = formulaToUpdate.variables.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap()

            val responseJsonObject = JsonObject(mapOf(
                "success" to JsonPrimitive(true),
                "data" to JsonObject(mapOf(
                    "id" to JsonPrimitive(formulaToUpdate.id),
                    "name" to JsonPrimitive(formulaToUpdate.name),
                    "formula" to JsonPrimitive(formulaToUpdate.formula),
                    "formulaEncrypted" to JsonPrimitive(formulaToUpdate.formulaEncrypted),
                    "variables" to JsonObject(variablesMap)
                ))
            ))

            context.res.setBodyText(json.encodeToString(responseJsonObject))
        } else {
            throw Exception("No se pudo actualizar la fórmula. Posiblemente no existe.")
        }
    } catch (e: Exception) {
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
 * CORREGIDO: Ruta simplificada y método HTTP especificado
 */
@Api(routeOverride = "DELETE formulas/{id}")
suspend fun deleteAndroidFormula(context: ApiContext) {
    try {
        // Verificar que la petición es un DELETE
        if (context.req.method.toString().lowercase() != "delete") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteFormula(id)

        if (success) {
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

