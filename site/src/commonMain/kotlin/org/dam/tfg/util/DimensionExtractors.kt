package org.dam.tfg.util

object DimensionExtractors {
    fun extractCubetaDimensions(nombre: String): Pair<Double, Double> {
        // Para cubetas circulares (diámetro)
        val regexDiametro = Regex("Diametro (\\d+)x(\\d+)")
        regexDiametro.find(nombre)?.let {
            val diametro = it.groupValues[1].toDoubleOrNull() ?: 0.0
            return Pair(diametro, diametro)
        }

        // Para cubetas rectangulares o cuadradas (LxAxA)
        val regexDimensiones = Regex("(\\d+)[xX×](\\d+)[xX×](\\d+)")
        regexDimensiones.find(nombre)?.let {
            val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
            val ancho = it.groupValues[2].toDoubleOrNull() ?: 0.0
            return Pair(largo, ancho)
        }

        // Formato alternativo con × o X (para dimensiones de 2 valores)
        val regexAlternativo2D = Regex("(\\d+)[×Xx](\\d+)")
        regexAlternativo2D.find(nombre)?.let {
            val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
            val ancho = it.groupValues[2].toDoubleOrNull() ?: 0.0
            return Pair(largo, ancho)
        }

        // Extracción manual de números si todos los regex fallan
        val numeros = Regex("\\d+").findAll(nombre).map { it.value.toDoubleOrNull() ?: 0.0 }.toList()
        if (numeros.size >= 2) {
            return Pair(numeros[0], numeros[1])
        }

        // Si no se puede extraer, devolver dimensiones mínimas
        return Pair(0.0, 0.0)
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