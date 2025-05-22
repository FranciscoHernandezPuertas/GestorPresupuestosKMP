package org.dam.tfg.androidapp.api.model

import kotlinx.serialization.Serializable

/**
 * Modelo de respuesta estándar para todas las APIs
 * @param success Indica si la operación fue exitosa
 * @param data Datos de respuesta (opcional)
 * @param error Mensaje de error en caso de fallo (opcional)
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)
