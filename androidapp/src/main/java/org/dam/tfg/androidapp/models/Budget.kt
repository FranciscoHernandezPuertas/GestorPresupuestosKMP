package org.dam.tfg.androidapp.models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

@Serializable
data class Budget(
    val _id: String = IdUtils.generateId(),
    val tipo: String,
    val tramos: List<Tramo>,
    val elementosGenerales: List<ElementoGeneral>,
    val cubetas: List<Cubeta>,
    val modulos: List<Modulo>,
    val precioTotal: Long,
    val fechaCreacion: String,
    val username: String,
    val error: String = ""
)

@Serializable
data class Tramo(
    val numero: Int,
    val largo: Int,
    val ancho: Int,
    val precio: Long,
    val tipo: String,
    val error: String = ""
)

@Serializable
data class ElementoGeneral(
    val nombre: String,
    val cantidad: Int,
    val precio: Long,
    val limite: Limite
)

@Serializable
data class Limite(
    val id: String,
    val name: String,
    val minQuantity: Int,
    val maxQuantity: Int,
    val initialQuantity: Int
)

@Serializable
data class Cubeta(
    val tipo: String,
    val numero: Int,
    val largo: Int,
    val fondo: Int,
    val alto: Int,
    val precio: Long,
    val error: String = "",
    val minQuantity: Int
)

@Serializable
data class Modulo(
    val nombre: String,
    val largo: Int,
    val fondo: Int,
    val alto: Int,
    val cantidad: Int,
    val limite: Limite,
    val precio: Long
)
