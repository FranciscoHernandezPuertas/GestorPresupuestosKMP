package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

@Serializable
data class Formula(
    @SerialName("_id")
    val _id: String = IdUtils.generateId(),

    // Agregamos una propiedad para manejar "id" y convertirlo a "_id" si es necesario
    @SerialName("id")
    private val id: String? = null,

    val name: String = "",
    val formula: String = "", // Formula encriptada o desencriptada
    val formulaEncrypted: Boolean = true,
    val variables: Map<String, String> = emptyMap()
) {
    fun getActualId(): String {
        return _id.ifEmpty { id ?: "" }
    }
}
