package org.dam.tfg.androidapp.models

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class History(
    val _id: String,
    val userId: String,
    val action: String,
    val timestamp: String,
    val details: String
)
