package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0
)
