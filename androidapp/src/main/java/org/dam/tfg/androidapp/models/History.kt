package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class History(
    @SerialName("_id")
    val id: String = "",
    val userId: String = "",
    val action: String = "",
    val timestamp: String = "",
    val details: String = ""
)
