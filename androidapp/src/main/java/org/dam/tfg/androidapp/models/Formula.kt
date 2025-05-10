package org.dam.tfg.androidapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Formula(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val formula: String = "",
    val formulaEncrypted: Boolean = true,
    val variables: Map<String, String> = mapOf()
)
