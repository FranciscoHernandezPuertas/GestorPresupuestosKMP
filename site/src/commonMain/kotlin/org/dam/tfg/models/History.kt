package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
expect class History {
    val id: String
    val userId: String
    val action: String
    val timestamp: String
    val details: String

    fun toMap(): Map<String, Any>
    fun isValid(): Boolean
}