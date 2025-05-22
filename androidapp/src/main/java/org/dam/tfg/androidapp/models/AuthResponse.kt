package org.dam.tfg.androidapp.models

import kotlinx.serialization.Serializable

/**
 * Modelo para la respuesta de autenticación
 */
@Serializable
data class AuthResponse(
    val user: UserWithoutPassword,
    val token: String
)

/**
 * Modelo de usuario sin contraseña para respuestas de API
 */
@Serializable
data class UserWithoutPassword(
    val id: String,
    val username: String,
    val type: String
)
