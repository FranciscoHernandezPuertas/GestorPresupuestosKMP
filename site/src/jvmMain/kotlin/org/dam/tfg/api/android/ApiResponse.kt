package org.dam.tfg.api.android

import kotlinx.serialization.Serializable

/**
 * Clase común para estructurar las respuestas de la API para Android
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)
