package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Mesa(
    @SerialName("_id")
    val id: String = "",
    val tipo: String = "",
    val tramos: List<Tramo> = listOf(),
    val elementosGenerales: List<ElementoSeleccionado> = listOf(),
    val cubetas: List<Cubeta> = listOf(),
    val modulos: List<Modulo> = listOf(),
    val precioTotal: Double = 0.0,
    val fechaCreacion: String? = null,
    val username: String = "",
    var error: String = ""
)

@Serializable
data class Tramo(
    val numero: Int = 0,
    val largo: Int = 0,
    val ancho: Int = 0,
    val precio: Double = 0.0,
    val tipo: String = "",
    val error: String = ""
) {
    fun superficie(): Double = largo * ancho / 10000.0 // Convertir a m²
    
    fun isValid(): Boolean = largo > 0 && ancho > 0
}

@Serializable
data class ElementoSeleccionado(
    val nombre: String = "",
    val cantidad: Int = 0,
    val precio: Double = 0.0,
    val limite: Limite = Limite()
)

@Serializable
data class Limite(
    val id: String = "",
    val name: String = "",
    val minQuantity: Int = 0,
    val maxQuantity: Int = Int.MAX_VALUE,
    val initialQuantity: Int = 0
)

@Serializable
data class Cubeta(
    val tipo: String = "",
    val numero: Int = 0,
    val largo: Int = 0,
    val fondo: Int = 0,
    val alto: Int = 0,
    val precio: Double = 0.0,
    val error: String = "",
    val minQuantity: Int = 0
)

@Serializable
data class Modulo(
    val nombre: String = "",
    val largo: Int = 0,
    val fondo: Int = 0,
    val alto: Int = 0,
    val cantidad: Int = 0,
    val limite: Limite = Limite(),
    val precio: Double = 0.0
)
