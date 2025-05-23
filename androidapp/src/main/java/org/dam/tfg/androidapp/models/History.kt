package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class History(
    // Usamos campos opcionales para manejar cualquier formato de respuesta
    @SerialName("_id")
    val _id: String = "",

    // Agregamos una propiedad para manejar "id" y convertirlo a "_id" si es necesario
    @SerialName("id")
    private val id: String? = null,

    val userId: String = "",
    val action: String = "",
    val timestamp: String = "",
    val details: String = ""
) {
    fun getActualId(): String {
        return _id.ifEmpty { id ?: "" }
    }
}
