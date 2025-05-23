package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

// Asegurar que Budget use IDs normalizados
@Serializable
data class Budget(
    @SerialName("_id")
    val _id: String = IdUtils.generateId(),

    // Agregamos una propiedad para manejar "id" y convertirlo a "_id" si es necesario
    @SerialName("id")
    private val id: String? = null,

    val tipo: String = "",
    val tramos: List<Tramo> = emptyList(),
    val elementosGenerales: List<ElementoGeneral> = emptyList(),
    val cubetas: List<Cubeta> = emptyList(),
    val modulos: List<Modulo> = emptyList(),
    val precioTotal: Double = 0.0,
    val fechaCreacion: String = "",
    val username: String = "",
    val error: String = ""
) {
    fun getActualId(): String {
        return _id.ifEmpty { id ?: "" }
    }
}

@Serializable
data class Tramo(
    val numero: Int = 0,
    // Cambiamos a Double para permitir valores decimales
    val largo: Double = 0.0,
    val ancho: Double = 0.0,
    val precio: Double = 0.0,
    val tipo: String = "",
    val error: String = ""
)

@Serializable
data class ElementoGeneral(
    val nombre: String = "",
    val cantidad: Int = 0,
    val precio: Double = 0.0,
    val limite: Limite = Limite()
)

@Serializable
data class Limite(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val minQuantity: Int = 0,
    val maxQuantity: Int = 0,
    val initialQuantity: Int = 0
)

@Serializable
data class Cubeta(
    val tipo: String = "",
    val numero: Int = 0,
    val largo: Double = 0.0,
    val fondo: Double = 0.0,
    val alto: Double = 0.0,
    val precio: Double = 0.0,
    val error: String = "",
    val minQuantity: Int = 0
)

@Serializable
data class Modulo(
    val nombre: String = "",
    val largo: Double = 0.0,
    val fondo: Double = 0.0,
    val alto: Double = 0.0,
    val cantidad: Int = 0,
    val limite: Limite = Limite(),
    val precio: Double = 0.0
)
