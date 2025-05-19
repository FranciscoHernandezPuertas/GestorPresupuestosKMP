package org.dam.tfg.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.table.Mesa
import kotlin.math.absoluteValue

@Api(routeOverride = "budget/validate")
suspend fun validateBudget(context: ApiContext) {
    try {
        // Decodificar datos de la mesa
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos para la validación")

        val mesa = try {
            Json.decodeFromString(Mesa.serializer(), bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de la mesa: ${e.message}")
        }

        // Obtener formulas y materiales de la base de datos para el cálculo
        val mongoDB = context.data.getValue<MongoDB>()
        val formulas = mongoDB.getAllFormulas()
        val materiales = mongoDB.getAllMaterials()

        // Recalcular el presupuesto para garantizar que los valores son correctos
        val resultado = calculateFullBudget(mesa, formulas, materiales, context)

        // Verificar si hay discrepancias significativas entre el precio enviado y el calculado
        val diferenciaPorcentual = if (mesa.precioTotal > 0) {
            ((resultado.precioTotal - mesa.precioTotal) / mesa.precioTotal * 100).absoluteValue
        } else 0.0

        // Si la diferencia es mayor al 1%, se considera manipulación del cliente
        if (diferenciaPorcentual > 1.0) {
            context.logger.warn("Posible manipulación de precios detectada: ${mesa.precioTotal} vs ${resultado.precioTotal}")
        }

        // Devolver el resultado validado
        context.res.setBodyText(Json.encodeToString(resultado))
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            Json.encodeToString(
                ErrorResponse("Error al validar el presupuesto: ${e.message}")
            )
        )
    }
}
