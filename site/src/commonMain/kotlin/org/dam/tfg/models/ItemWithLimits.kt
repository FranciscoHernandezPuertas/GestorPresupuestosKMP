package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
data class ItemWithLimits(
    val id: String? = "",
    val name: String,
    val minQuantity: Int = 0,
    val maxQuantity: Int = Int.MAX_VALUE,
    val initialQuantity: Int? = 0
)