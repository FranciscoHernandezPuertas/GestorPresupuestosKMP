package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("_id")
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val type: String = "user"
)

@Serializable
data class UserWithoutPassword(
    @SerialName("_id")
    val id: String = "",
    val username: String = "",
    val type: String = "user"
)

@Serializable
data class AuthResponse(
    val user: UserWithoutPassword,
    val token: String
)

@Serializable
data class ErrorResponse(
    val message: String
)
