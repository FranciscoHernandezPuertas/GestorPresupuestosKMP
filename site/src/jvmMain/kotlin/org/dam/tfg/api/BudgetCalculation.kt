package org.dam.tfg.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.util.FormulaCalculator
import org.dam.tfg.util.FormulaEncryption

@Api(routeOverride = "budget/calculate")
suspend fun calculateBudget(context: ApiContext) {
    try {
        // Decodificar datos de la mesa
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos para el cálculo")

        val mesa = try {
            Json.decodeFromString(Mesa.serializer(), bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de la mesa: ${e.message}")
        }

        // Obtener fórmulas y materiales de la base de datos
        val mongoDB = context.data.getValue<MongoDB>()
        val formulas = mongoDB.getAllFormulas()
        val materiales = mongoDB.getAllMaterials()

        // Calcular el presupuesto
        val resultado = calculateFullBudget(mesa, formulas, materiales, context)

        // Devolver el resultado
        context.res.setBodyText(Json.encodeToString(resultado))
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            Json.encodeToString(
                ErrorResponse("Error al calcular el presupuesto: ${e.message}")
            )
        )
    }
}

@Serializable
data class BudgetResult(
    val precioTotal: Double,
    val desglose: Map<String, Double>
)

suspend fun calculateFullBudget(
    mesa: Mesa,
    formulas: List<Formula>,
    materiales: List<Material>,
    context: ApiContext
): BudgetResult {
    val desglose = mutableMapOf<String, Double>()
    var precioTotal = 0.0

    // Añadir logs para depuración
    context.logger.info("Calculando presupuesto para mesa tipo: ${mesa.tipo}")
    context.logger.info("Número de tramos: ${mesa.tramos.size}")
    context.logger.info("Número de cubetas: ${mesa.cubetas.size}")
    context.logger.info("Número de módulos: ${mesa.modulos.size}")
    context.logger.info("Número de elementos generales: ${mesa.elementosGenerales.size}")

    // Calcular precio de tramos
    mesa.tramos.forEachIndexed { index, tramo ->
        val precioTramo = calcularPrecioTramo(tramo, formulas, materiales)
        desglose["tramo_$index"] = precioTramo
        precioTotal += precioTramo
        context.logger.info("Precio tramo $index: $precioTramo")
    }

    // Calcular precio de cubetas
    mesa.cubetas.forEachIndexed { index, cubeta ->
        val precioCubeta = calcularPrecioCubeta(cubeta, formulas, materiales)
        desglose["cubeta_$index"] = precioCubeta
        precioTotal += precioCubeta
        context.logger.info("Precio cubeta $index: $precioCubeta")
    }

    // Calcular precio de módulos
    mesa.modulos.forEachIndexed { index, modulo ->
        val precioModulo = calcularPrecioModulo(modulo, formulas, materiales)
        desglose["modulo_$index"] = precioModulo
        precioTotal += precioModulo
        context.logger.info("Precio módulo $index: $precioModulo")
    }

    // Calcular precio de elementos generales usando la fórmula correspondiente
    mesa.elementosGenerales.forEach { elemento ->
        val precioElemento = calcularPrecioElementoGeneral(elemento, formulas, materiales)
        desglose["elemento_${elemento.nombre}"] = precioElemento
        precioTotal += precioElemento
        context.logger.info("Precio elemento ${elemento.nombre}: $precioElemento")
    }

    context.logger.info("Precio total calculado: $precioTotal")
    return BudgetResult(precioTotal, desglose)
}

private fun calcularPrecioTramo(
    tramo: org.dam.tfg.models.table.Tramo,
    formulas: List<Formula>,
    materiales: List<Material>
): Double {
    // Buscar la fórmula para los tramos
    val formulaTramo = formulas.find { it.name.contains("tramo", ignoreCase = true) }
        ?: return tramo.precio // Si no hay fórmula específica, usar el precio por defecto

    // Desencriptar la fórmula si es necesario
    val formulaText = if (formulaTramo.formulaEncrypted) {
        FormulaEncryption.decrypt(formulaTramo.formula)
    } else {
        formulaTramo.formula
    }

    // Crear variables para la evaluación de la fórmula
    val variables = mutableMapOf<String, Double>()
    variables["largo"] = tramo.largo
    variables["ancho"] = tramo.ancho
    variables["areaTramo"] = tramo.largo * tramo.ancho
    variables["areaTotal"] = tramo.largo * tramo.ancho // Para compatibilidad con la fórmula "2*areaTotal"
    variables["superficie"] = tramo.largo * tramo.ancho

    // Valores predeterminados en caso de no encontrar materiales específicos
    variables["acero"] = 100.0
    variables["tiempoTrabajo"] = 50.0
    variables["tipoTramo"] = when (tramo.tipo.toString()) {
        "MURAL" -> 1.5
        "CENTRAL" -> 1.0
        "ANGULAR" -> 2.0
        else -> 1.0
    }

    // Añadir los precios de materiales a las variables
    materiales.forEach { material ->
        val nombreVariable = material.name.lowercase().replace(" ", "_")
        variables[nombreVariable] = material.price
    }

    // Evaluar la fórmula y asegurar que devuelve un valor positivo
    val precio = FormulaCalculator.evaluateFormula(formulaText, variables)
    return if (precio > 0) precio else tramo.precio // Si el cálculo falla, usar el precio predeterminado
}

// Funciones similares para cubetas y módulos
private fun calcularPrecioCubeta(
    cubeta: org.dam.tfg.models.table.Cubeta,
    formulas: List<Formula>,
    materiales: List<Material>
): Double {
    val formulaCubeta = formulas.find { it.name.contains("cubeta", ignoreCase = true) }
        ?: return cubeta.precio

    val formulaText = if (formulaCubeta.formulaEncrypted) {
        FormulaEncryption.decrypt(formulaCubeta.formula)
    } else {
        formulaCubeta.formula
    }

    val variables = mutableMapOf<String, Double>()
    variables["largo"] = cubeta.largo
    variables["fondo"] = cubeta.fondo
    variables["alto"] = cubeta.alto ?: 0.0
    variables["volumen"] = cubeta.largo * cubeta.fondo * (cubeta.alto ?: 0.0)
    variables["volumenCubeta"] = cubeta.largo * cubeta.fondo * (cubeta.alto ?: 0.0) // Para compatibilidad con "0.2*volumenCubeta+acero+tiempoTrabajo"

    // Valores predeterminados
    variables["acero"] = 200.0
    variables["tiempoTrabajo"] = 75.0

    materiales.forEach { material ->
        val nombreVariable = material.name.lowercase().replace(" ", "_")
        variables[nombreVariable] = material.price
    }

    val precio = FormulaCalculator.evaluateFormula(formulaText, variables)
    return if (precio > 0) precio else cubeta.precio
}

private fun calcularPrecioModulo(
    modulo: org.dam.tfg.models.table.Modulo,
    formulas: List<Formula>,
    materiales: List<Material>
): Double {
    val formulaModulo = formulas.find { it.name.contains("modulo", ignoreCase = true) }
        ?: return modulo.precio * modulo.cantidad

    val formulaText = if (formulaModulo.formulaEncrypted) {
        FormulaEncryption.decrypt(formulaModulo.formula)
    } else {
        formulaModulo.formula
    }

    val variables = mutableMapOf<String, Double>()
    variables["largo"] = modulo.largo
    variables["fondo"] = modulo.fondo
    variables["alto"] = modulo.alto
    variables["cantidad"] = modulo.cantidad.toDouble()
    variables["volumen"] = modulo.largo * modulo.fondo * modulo.alto

    // Valores específicos para tipos de módulos (por ejemplo: "2*tipoModulo")
    variables["tipoModulo"] = when {
        modulo.nombre.contains("armario", ignoreCase = true) -> 300.0
        modulo.nombre.contains("cajón", ignoreCase = true) -> 200.0
        modulo.nombre.contains("estante", ignoreCase = true) -> 150.0
        else -> 100.0
    }

    materiales.forEach { material ->
        val nombreVariable = material.name.lowercase().replace(" ", "_")
        variables[nombreVariable] = material.price
    }

    val precioPorUnidad = FormulaCalculator.evaluateFormula(formulaText, variables)
    return if (precioPorUnidad > 0) precioPorUnidad * modulo.cantidad else modulo.precio * modulo.cantidad
}

// Nueva función para calcular el precio de los elementos generales
private fun calcularPrecioElementoGeneral(
    elemento: org.dam.tfg.models.table.ElementoSeleccionado,
    formulas: List<Formula>,
    materiales: List<Material>
): Double {
    // Buscar la fórmula para los elementos generales
    val formulaElemento = formulas.find { it.name.contains("elemento", ignoreCase = true) }
        ?: return elemento.precio * elemento.cantidad // Si no hay fórmula específica, usar el precio por defecto

    // Desencriptar la fórmula si es necesario
    val formulaText = if (formulaElemento.formulaEncrypted) {
        FormulaEncryption.decrypt(formulaElemento.formula)
    } else {
        formulaElemento.formula
    }

    // Crear variables para la evaluación de la fórmula
    val variables = mutableMapOf<String, Double>()

    // Determinar el tipo de elemento basado en el nombre normalizado
    val nombreNormalizado = elemento.nombre.lowercase().replace(" ", "")

    // Añadir el valor del tipo de elemento desde la fórmula
    val tipoElementoKey = formulaElemento.variables.keys.find {
        it.lowercase().contains(nombreNormalizado) || nombreNormalizado.contains(it.lowercase().replace("tipo", ""))
    }

    val valorTipoElemento = if (tipoElementoKey != null) {
        formulaElemento.variables[tipoElementoKey]?.toDoubleOrNull() ?: 10.0
    } else {
        // Valor predeterminado si no se encuentra el tipo
        10.0
    }

    // Añadir el valor específico para este elemento
    variables["tipoElemento"] = valorTipoElemento
    variables["cantidad"] = elemento.cantidad.toDouble()

    // Añadir otras variables genéricas que podrían ser útiles
    variables["precio"] = elemento.precio

    // Añadir los precios de materiales a las variables
    materiales.forEach { material ->
        val nombreVariable = material.name.lowercase().replace(" ", "_")
        variables[nombreVariable] = material.price
    }

    // Evaluar la fórmula
    val precioPorUnidad = FormulaCalculator.evaluateFormula(formulaText, variables)

    // Aplicar la cantidad
    return if (precioPorUnidad > 0) precioPorUnidad * elemento.cantidad else elemento.precio * elemento.cantidad
}
