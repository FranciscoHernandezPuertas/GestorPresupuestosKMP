package org.dam.tfg.androidapp.models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

// Asegurar que Material use IDs normalizados
@Serializable
data class Material(
    val _id: String = IdUtils.generateId(),
    val name: String,
    val price: Double
)
