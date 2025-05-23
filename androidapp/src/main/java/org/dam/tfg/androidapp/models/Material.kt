package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

@Serializable
data class Material(
    @SerialName("_id")
    val _id: String = IdUtils.generateId(),

    // Agregamos una propiedad para manejar "id" y convertirlo a "_id" si es necesario
    @SerialName("id")
    private val id: String? = null,

    val name: String = "",
    val price: Double = 0.0
) {
    fun getActualId(): String {
        val effectiveId = when {
            _id.isNotBlank() -> _id
            id != null && id.isNotBlank() -> id
            else -> IdUtils.generateId()
        }
        return IdUtils.normalizeId(effectiveId)
    }
}
