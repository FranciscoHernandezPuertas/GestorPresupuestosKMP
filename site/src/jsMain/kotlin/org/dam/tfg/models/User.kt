package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual data class User (
    @SerialName("_id") // Asegura que el campo se llame "_id" en MongoDB
    actual val id: String = "",
    actual val username: String = "",
    actual val password: String = "",
    actual val type: String = "user"
)

@Serializable
actual data class UserWithoutPassword(
    @SerialName("_id") // Asegura que el campo se llame "_id" en MongoDB
    actual val id: String = "",
    actual val username: String = "",
    actual val type: String = "user"
)