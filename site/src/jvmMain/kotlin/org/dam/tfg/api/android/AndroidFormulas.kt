package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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
@Api(routeOverride = "/formulas")
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

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = resultFormulas
                )
            )
        )
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
 */
@Api(routeOverride = "/formulas/{id}")
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

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = resultFormula
                )
            )
        )
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
 */
@Api(routeOverride = "/formulas")
suspend fun createAndroidFormula(context: ApiContext) {
    try {
        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de la fórmula")

        val formulaDto = try {
            json.decodeFromString<Formula>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de la fórmula: ${e.message}")
        }

        // Validaciones básicas
        if (formulaDto.name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formulaDto.formula.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        // Encriptar la fórmula si no está encriptada
        val encryptedFormula = if (!formulaDto.formulaEncrypted) {
            FormulaEncryption.encrypt(formulaDto.formula)
        } else {
            formulaDto.formula
        }

        // Crear fórmula con ID generado si no tiene uno
        val newFormula = Formula(
            id = if (formulaDto.id.isBlank()) ObjectId().toHexString() else formulaDto.id,
            name = formulaDto.name.trim(),
            formula = encryptedFormula,
            formulaEncrypted = true, // Siempre marcamos como encriptada
            variables = formulaDto.variables
        )

        val success = context.data.getValue<MongoDB>().addFormula(newFormula)

        if (success) {
            context.res.status = 201 // Created
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Formula>(
                        success = true,
                        data = newFormula
                    )
                )
            )
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
 */
@Api(routeOverride = "/formulas/{id}")
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

        val formulaDto = try {
            json.decodeFromString<Formula>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de la fórmula: ${e.message}")
        }

        // Validaciones
        if (formulaDto.name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formulaDto.formula.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        // Encriptar la fórmula si no está encriptada
        val encryptedFormula = if (!formulaDto.formulaEncrypted) {
            FormulaEncryption.encrypt(formulaDto.formula)
        } else {
            formulaDto.formula
        }

        // Asegurar que el ID en el path coincida con el ID en el cuerpo
        val formulaToUpdate = Formula(
            id = id,
            name = formulaDto.name.trim(),
            formula = encryptedFormula,
            formulaEncrypted = true, // Siempre marcamos como encriptada
            variables = formulaDto.variables
        )

        val success = context.data.getValue<MongoDB>().updateFormula(formulaToUpdate)

        if (success) {
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Formula>(
                        success = true,
                        data = formulaToUpdate
                    )
                )
            )
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
 */
@Api(routeOverride = "/formulas/{id}")
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
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Boolean>(
                        success = true,
                        data = true
                    )
                )
            )
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
