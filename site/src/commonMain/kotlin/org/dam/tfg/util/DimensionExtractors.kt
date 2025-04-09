package org.dam.tfg.util

object DimensionExtractors {
    // Función actualizada para extraer largo, fondo y alto
    fun extractCubetaDimensions(nombre: String): Triple<Double, Double, Double> {
        // Para formato LxFxA (3 dimensiones)
        val regexDimensiones3D = Regex("(\\d+)[xX×](\\d+)[xX×](\\d+)")
        regexDimensiones3D.find(nombre)?.let {
            val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
            val fondo = it.groupValues[2].toDoubleOrNull() ?: 0.0
            val alto = it.groupValues[3].toDoubleOrNull() ?: 0.0
            return Triple(largo, fondo, alto)
        }

        // Para cubetas circulares (diámetro)
        val regexDiametro = Regex("Diametro (\\d+)x(\\d+)")
        regexDiametro.find(nombre)?.let {
            val diametro = it.groupValues[1].toDoubleOrNull() ?: 0.0
            val alto = it.groupValues[2].toDoubleOrNull() ?: 0.0
            return Triple(diametro, diametro, alto)
        }

        // Formato alternativo con × o X (para dimensiones de 2 valores)
        val regexAlternativo2D = Regex("(\\d+)[×Xx](\\d+)")
        regexAlternativo2D.find(nombre)?.let {
            val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
            val fondo = it.groupValues[2].toDoubleOrNull() ?: 0.0
            return Triple(largo, fondo, 0.0)
        }

        // Extracción manual de números si todos los regex fallan
        val numeros = Regex("\\d+").findAll(nombre).map { it.value.toDoubleOrNull() ?: 0.0 }.toList()
        if (numeros.size >= 3) {
            return Triple(numeros[0], numeros[1], numeros[2])
        } else if (numeros.size == 2) {
            return Triple(numeros[0], numeros[1], 0.0)
        }

        // Si no se puede extraer, devolver dimensiones mínimas
        return Triple(0.0, 0.0, 0.0)
    }

    fun extractGenericDimensions(nombre: String): Pair<Double, Double> {
        // Regex para formatos comunes: 800x600, 800×600, etc.
        val regexDimensiones = Regex("(\\d+)[xX×](\\d+)")
        regexDimensiones.find(nombre)?.let {
            val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
            val ancho = it.groupValues[2].toDoubleOrNull() ?: 0.0
            return Pair(largo, ancho)
        }

        return Pair(0.0, 0.0)
    }
}