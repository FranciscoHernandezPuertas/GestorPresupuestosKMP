package org.dam.tfg.androidapp.models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

// Asegurar que Formula use IDs normalizados
@Serializable
data class Formula(
    val _id: String = IdUtils.generateId(),
    val name: String,
    val formula: String, // JWT encrypted formula
    val formulaEncrypted: Boolean = true,
    val variables: Map<String, String> = emptyMap()
)
