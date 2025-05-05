package org.dam.tfg.models.table

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
actual class Mesa(
    @SerialName("_id")
    actual val id: String = ObjectId().toHexString(),
    actual val tipo: String,
    actual val tramos: List<Tramo>,
    actual val elementosGenerales: List<ElementoSeleccionado>,
    actual val cubetas: List<Cubeta>,
    actual val modulos: List<Modulo>,
    actual val precioTotal: Double,
    actual val fechaCreacion: String?,
    actual val username: String,
    actual var error: String
) {

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