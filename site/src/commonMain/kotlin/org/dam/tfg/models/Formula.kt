package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
@Serializable
expect class Formula {
    val id: String
    val name: String
    val formula: String  // Esta será encriptada/desencriptada según necesidad
    val formulaEncrypted: Boolean // Para indicar si la fórmula está encriptada
    val variables: Map<String, String>

    fun toMap(): Map<String, Any>
}