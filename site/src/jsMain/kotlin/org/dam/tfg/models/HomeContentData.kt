package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
data class HomeContentData(
    val title: String,
    val description: String,
)