package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
sealed class Extra {
    abstract val tipo: String
    abstract val numero: Int
    abstract val largo: Double?
    abstract val ancho: Double?
    abstract val alto: Double? // opcional, en milímetros
    abstract val precio: Double
    abstract fun calcularPrecio(): Double
    abstract fun isValid(): Boolean
    abstract var error: String
}

@Serializable
data class ElementosGenerales (
    override val tipo: String = "ElementosGenerales",
    val nombre: String = "",
    override val numero: Int = 0,
    override val largo: Double? = null,
    override val ancho: Double? = null,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        error = ""
        return true
    }
}