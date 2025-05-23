package org.dam.tfg.androidapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

@Parcelize
@Serializable
data class User(
    @SerialName("_id")
    val _id: String = IdUtils.generateId(),

    // Agregamos una propiedad para manejar "id" y convertirlo a "_id" si es necesario
    @SerialName("id")
    private val id: String? = null,

    val username: String = "",
    val password: String = "", // SHA-256 hash o texto plano (para enviar al servidor)
    val type: String = "user"
) : Parcelable {
    fun getActualId(): String {
        return _id.ifEmpty { id ?: "" }
    }
}
