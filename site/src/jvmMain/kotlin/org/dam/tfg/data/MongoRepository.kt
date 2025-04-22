package org.dam.tfg.data

import org.dam.tfg.models.Formula
import org.dam.tfg.models.User
import org.dam.tfg.models.Material
import org.dam.tfg.models.table.Mesa

interface MongoRepository {
    suspend fun checkUserExistence(user: User): User?
    suspend fun checkUserId(id: String): Boolean

    suspend fun addUser(user: User): Boolean
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(id: String): Boolean
    suspend fun getUserById(id: String): User?
    suspend fun getAllUsers(): List<User>

    suspend fun addMaterial(material: Material): Boolean
    suspend fun updateMaterial(material: Material): Boolean
    suspend fun deleteMaterial(id: String): Boolean
    suspend fun getMaterialById(id: String): Material?
    suspend fun getAllMaterials(): List<Material>

    suspend fun addFormula(formula: Formula): Boolean
    suspend fun updateFormula(formula: Formula): Boolean
    suspend fun deleteFormula(id: String): Boolean
    suspend fun getFormulaById(id: String): Formula?
    suspend fun getAllFormulas(): List<Formula>

    suspend fun addMesa(mesa: Mesa): Boolean
    suspend fun updateMesa(mesa: Mesa): Boolean
    suspend fun deleteMesa(id: String): Boolean
    suspend fun getMesaById(id: String): Mesa?
    suspend fun getAllMesas(): List<Mesa>
}