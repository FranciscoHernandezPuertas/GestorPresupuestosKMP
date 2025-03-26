package org.dam.tfg.util

import androidx.compose.runtime.mutableStateOf
import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.budget.Mesa
import org.dam.tfg.models.budget.Material
import org.dam.tfg.models.budget.Tramo
import org.dam.tfg.models.budget.Extra
import org.w3c.dom.get
import org.w3c.dom.set

object BudgetManager {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // Guarda una mesa en localStorage
    fun saveMesa(mesa: Mesa) {
        try {
            val mesaJson = json.encodeToString(mesa)
            localStorage["current_mesa"] = mesaJson
        } catch (e: Exception) {
            console.error("Error al guardar mesa: ${e.message}")
        }
    }

    // Recupera una mesa desde localStorage
    fun loadMesa(): Mesa {
        return try {
            val mesaJson = localStorage["current_mesa"]
            if (mesaJson != null) {
                json.decodeFromString(mesaJson)
            } else {
                Mesa()
            }
        } catch (e: Exception) {
            console.error("Error al recuperar mesa: ${e.message}")
            Mesa()
        }
    }

    // Limpia los datos guardados
    fun clearMesa() {
        localStorage.removeItem("current_mesa")
    }

    // Estado actual de la mesa que se está configurando
    val currentMesa = mutableStateOf(loadMesa())

    // Actualiza la mesa y la guarda en localStorage
    fun updateMesa(newMesa: Mesa) {
        currentMesa.value = newMesa
        saveMesa(newMesa)
    }

    // Valida la configuración de la mesa
    fun validateMesa(): Boolean {
        return currentMesa.value.isValid()
    }
}