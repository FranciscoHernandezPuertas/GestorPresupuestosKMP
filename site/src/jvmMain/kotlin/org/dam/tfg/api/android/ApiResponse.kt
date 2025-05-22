package org.dam.tfg.api.android

/**
 * Clase común para estructurar las respuestas de la API para Android
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)
