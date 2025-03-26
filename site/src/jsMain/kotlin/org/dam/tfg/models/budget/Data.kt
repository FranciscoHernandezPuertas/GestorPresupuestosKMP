package org.dam.tfg.models.budget

import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: String = "",
    val nombre: String = "",
    val precioPorMilimetroCuadrado: Double = 0.0
)

@Serializable
data class Tramo(
    val numero: Int = 0,
    val largo: Double = 0.0,
    val ancho: Double = 0.0,
    var error: String = ""
) {
    fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }

    fun superficie(): Double = largo * ancho
}

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

@Serializable
data class Peto(
    override val tipo: String = "Peto",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    val material: Material? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        if (material == null) {
            error = "Debe seleccionar un material"
            return false
        }
        error = ""
        return true
    }
}
/*
@Serializable
data class Seno(
    override val tipo: String = "Seno",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    val profundidad: Double = 0.0,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        if (profundidad <= 0) {
            error = "La profundidad debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}
*/
// Extra: SoporteParaBandejas
@Serializable
data class SoporteParaBandejas(
    override val tipo: String = "SoporteParaBandejas",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: Cubeta
@Serializable
data class Cubeta(
    override val tipo: String = "Cubeta",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: Panel
@Serializable
data class Panel(
    override val tipo: String = "Panel",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: Estante
@Serializable
data class Estante(
    override val tipo: String = "Estante",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: Cajón
@Serializable
data class Cajon(
    override val tipo: String = "Cajón",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: SoporteParaBatidoras
@Serializable
data class SoporteParaBatidoras(
    override val tipo: String = "SoporteParaBatidoras",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: Cajonera
@Serializable
data class Cajonera(
    override val tipo: String = "Cajonera",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: SoporteFrontalParaBotellas
@Serializable
data class SoporteFrontalParaBotellas(
    override val tipo: String = "SoporteFrontalParaBotellas",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: CubaParaHielo
@Serializable
data class CubaParaHielo(
    override val tipo: String = "CubaParaHielo",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

// Extra: CubaParaDesperdicios
@Serializable
data class CubaParaDesperdicios(
    override val tipo: String = "CubaParaDesperdicios",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = ""
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}

@Serializable
data class Mesa(
    val id: String = "",
    val tipo: String = "",
    val tramos: List<Tramo> = listOf(),
    val extras: List<Extra> = listOf(),
    val material: Material? = null,
    var precioTotal: Double = 0.0,
    var error: String = ""
) {
    fun calcularPrecio(): Double {
        if (material == null) return 0.0

        // Precio base de los tramos
        val precioTramos = tramos.sumOf { it.superficie() * material.precioPorMilimetroCuadrado }

        // Precio de los extras
        val precioExtras = extras.sumOf { it.calcularPrecio() }

        precioTotal = precioTramos + precioExtras
        return precioTotal
    }

    fun isValid(): Boolean {
        if (tipo.isEmpty()) {
            error = "Debe seleccionar un tipo de mesa"
            return false
        }

        if (material == null) {
            error = "Debe seleccionar un material"
            return false
        }

        if (tramos.isEmpty()) {
            error = "La mesa debe tener al menos un tramo"
            return false
        }

        if (!tramos.all { it.isValid() }) {
            error = "Uno o más tramos no son válidos"
            return false
        }

        if (!extras.all { it.isValid() }) {
            error = "Uno o más extras no son válidos"
            return false
        }

        error = ""
        return true
    }
}

@Serializable
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String>
)
