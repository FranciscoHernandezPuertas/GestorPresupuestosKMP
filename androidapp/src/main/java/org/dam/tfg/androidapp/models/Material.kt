package org.dam.tfg.androidapp.models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Material(
    val _id: String = ObjectId().toString(),
    val name: String,
    val price: Double
)
