package org.dam.tfg.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @Contextual
    val user: UserWithoutPassword,
    val token: String
)

@Serializable
data class ErrorResponse(
    val message: String
)

@Serializable
data class TokenValidationRequest(
    val token: String
)