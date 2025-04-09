package org.dam.tfg.repositories

import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.models.table.Tramo
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.collections.set

class BudgetRepositoryJs : BudgetRepository {
    override fun saveMesa(mesa: Mesa) {
        localStorage["mesa_data"] = Json.encodeToString(mesa)
    }

    override fun loadMesa(): Mesa {
        val mesaJson = localStorage["mesa_data"]
        return if (!mesaJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(mesaJson)
            } catch (e: Exception) {
                Mesa()
            }
        } else {
            Mesa()
        }
    }

    override fun saveTramos (tramos: List<Tramo>) {
        localStorage["tramos_data"] = Json.encodeToString(tramos)
    }

    override fun getTramos(): List<Tramo> {
        val tramosJson = localStorage["tramos_data"]
        return if (!tramosJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(tramosJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun setTipoMesa(tipo: String) {
        localStorage["tipo_mesa"] = tipo
    }
    override fun getTipoMesa(): String {
        return localStorage["tipo_mesa"] ?: ""
    }
    override fun setMaterial(material: String) {
        localStorage["material"] = material
    }
    override fun getMaterial(): String {
        return localStorage["material"] ?: ""
    }
    override fun setPrecioTotal(precioTotal: Double) {
        localStorage["precio_total"] = precioTotal.toString()
    }
    override fun getPrecioTotal(): Double {
        return localStorage["precio_total"]?.toDoubleOrNull() ?: 0.0
    }
    override fun setExtras(extras: List<String>) {
        localStorage["extras"] = JSON.stringify(extras)
    }
    override fun getExtras(): List<String> {
        val extrasJson = localStorage["extras"]
        return if (!extrasJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(extrasJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    override fun setCubeta(cubeta: String) {
        localStorage["cubeta"] = JSON.stringify(cubeta)
    }
    override fun getCubeta(): String {
        return localStorage["cubeta"] ?: ""
    }
    override fun setCubetaMaxQuantity(maxQuantity: Int) {
        localStorage["cubeta_max_quantity"] = maxQuantity.toString()
    }
    override fun getCubetaMaxQuantity(): Int {
        return localStorage["cubeta_max_quantity"]?.toIntOrNull() ?: 0
    }
    override fun setCubetaLargo(largo: Double) {
        localStorage["cubeta_largo"] = largo.toString()
    }
    override fun getCubetaLargo(): Double {
        return localStorage["cubeta_largo"]?.toDoubleOrNull() ?: 0.0
    }
    override fun setCubetaAncho(ancho: Double) {
        localStorage["cubeta_ancho"] = ancho.toString()
    }
    override fun getCubetaAncho(): Double {
        return localStorage["cubeta_ancho"]?.toDoubleOrNull() ?: 0.0
    }
    override fun setCubetaAlto(alto: Double) {
        localStorage["cubeta_alto"] = alto.toString()
    }
    override fun getCubetaAlto(): Double {
        return localStorage["cubeta_alto"]?.toDoubleOrNull() ?: 0.0
    }
    override fun setCubetaPrecio(precio: Double) {
        localStorage["cubeta_precio"] = precio.toString()
    }
    override fun getCubetaPrecio(): Double {
        return localStorage["cubeta_precio"]?.toDoubleOrNull() ?: 0.0
    }
    override fun setCubetaError(error: String) {
        localStorage["cubeta_error"] = error
    }
    override fun getCubetaError(): String {
        return localStorage["cubeta_error"] ?: ""
    }
    override fun setMesaError(error: String) {
        localStorage["mesa_error"] = error
    }
    override fun getMesaError(): String {
        return localStorage["mesa_error"] ?: ""
    }
    override fun setMesaId(id: String) {
        localStorage["mesa_id"] = id
    }
    override fun getMesaId(): String {
        return localStorage["mesa_id"] ?: ""
    }
    override fun setMesaTipo(tipo: String) {
        localStorage["mesa_tipo"] = tipo
    }
    override fun getMesaTipo(): String {
        return localStorage["mesa_tipo"] ?: ""
    }
    override fun setMesaTramos(tramos: List<Tramo>) {
        localStorage["mesa_tramos"] = Json.encodeToString(tramos)
    }
    override fun getMesaTramos(): List<Tramo> {
        val tramosJson = localStorage["mesa_tramos"]
        return if (!tramosJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(tramosJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    override fun setMesaExtras(extras: List<String>) {
        localStorage["mesa_extras"] = Json.encodeToString(extras)
    }
    override fun getMesaExtras(): List<String> {
        val extrasJson = localStorage["mesa_extras"]
        return if (!extrasJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(extrasJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun setMesaPrecioTotal(precioTotal: Double) {
        localStorage["mesa_precio_total"] = precioTotal.toString()
    }
    override fun getMesaPrecioTotal(): Double {
        return localStorage["mesa_precio_total"]?.toDoubleOrNull() ?: 0.0
    }
}