package org.dam.tfg.models

import org.bson.codecs.ObjectIdGenerator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
@Serializable
data class User(
    @SerialName("_id") // Asegura que el campo se llame "_id" en MongoDB
    val id: String = ObjectId().toHexString(), // Genera un nuevo ObjectId y lo convierte a String
    val username: String = "",
    val password: String = ""
)