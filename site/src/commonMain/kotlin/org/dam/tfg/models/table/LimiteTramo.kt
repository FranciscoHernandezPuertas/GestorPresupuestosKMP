package org.dam.tfg.models.table

data class LimiteTramo(
    val id: String = "",
    val numTramo: Int = 0,
    val minLargo: Double = 0.0,
    val maxLargo: Double = 0.0,
    val minAncho: Double = 0.0,
    val maxAncho: Double = 0.0
) {
    fun isValid(): Boolean {
        return minLargo > 0 && maxLargo > 0 && minAncho > 0 && maxAncho > 0 &&
                minLargo < maxLargo && minAncho < maxAncho
    }

    fun isDimensionValid(largo: Double, ancho: Double): Boolean {
        return largo in minLargo..maxLargo && ancho in minAncho..maxAncho
    }
}