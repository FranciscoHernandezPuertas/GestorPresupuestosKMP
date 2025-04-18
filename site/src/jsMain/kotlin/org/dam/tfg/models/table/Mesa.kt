package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
actual class Mesa(
    actual val id: String = "",
    actual val tipo: String,
    actual val tramos: List<Tramo>,
    actual val cubetas: List<Cubeta>,
    actual val modulos: List<ModuloSeleccionado>,
    actual val elementosGenerales: Map<String, Int>,
    actual val precioTotal: Double,
    actual var error: String
) {
    actual companion object {}

    actual fun isValid(): Boolean {
        if (tramos.isEmpty()) {
            error = "Una mesa debe tener al menos un tramo"
            return false
        }

        if (!tramos.all { it.isValid() }) {
            error = "Hay tramos inválidos"
            return false
        }

        error = ""
        return true
    }

    actual fun calcularSuperficieTotal(): Double {
        return tramos.sumOf { it.superficie() }
    }

    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "tipo" to tipo,
            "tramos" to tramos,
            "cubetas" to cubetas,
            "modulos" to modulos,
            "elementosGenerales" to elementosGenerales,
            "precioTotal" to precioTotal,
            "error" to error
        )
    }
}