package org.dam.tfg.repositories

import org.dam.tfg.models.table.Mesa
import org.dam.tfg.models.table.Tramo

interface BudgetRepository {
    fun saveMesa(mesa: Mesa)
    fun loadMesa(): Mesa
    fun saveTramos(tramos: List<Tramo>)
    fun getTramos(): List<Tramo>
    fun setTipoMesa(tipo: String)
    fun getTipoMesa(): String
    fun setMaterial(material: String)
    fun getMaterial(): String
    fun setPrecioTotal(precioTotal: Double)
    fun getPrecioTotal(): Double
    fun setExtras(extras: List<String>)
    fun getExtras(): List<String>
    fun setCubeta(cubeta: String)
    fun getCubeta(): String
    fun setCubetaMaxQuantity(maxQuantity: Int)
    fun getCubetaMaxQuantity(): Int
    fun setCubetaLargo(largo: Double)
    fun getCubetaLargo(): Double
    fun setCubetaAncho(ancho: Double)
    fun getCubetaAncho(): Double
    fun setCubetaAlto(alto: Double)
    fun getCubetaAlto(): Double
    fun setCubetaPrecio(precio: Double)
    fun getCubetaPrecio(): Double
    fun setCubetaError(error: String)
    fun getCubetaError(): String
    fun setMesaError(error: String)
    fun getMesaError(): String
    fun setMesaId(id: String)
    fun getMesaId(): String
    fun setMesaTipo(tipo: String)
    fun getMesaTipo(): String
    fun setMesaTramos(tramos: List<Tramo>)
    fun getMesaTramos(): List<Tramo>
    fun setMesaExtras(extras: List<String>)
    fun getMesaExtras(): List<String>
    fun setMesaMaterial(material: String)
    fun getMesaMaterial(): String
    fun setMesaPrecioTotal(precioTotal: Double)
    fun getMesaPrecioTotal(): Double
}