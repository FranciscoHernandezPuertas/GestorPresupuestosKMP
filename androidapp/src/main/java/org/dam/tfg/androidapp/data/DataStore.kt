package org.dam.tfg.androidapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.dam.tfg.androidapp.models.Formula
import org.dam.tfg.androidapp.models.History
import org.dam.tfg.androidapp.models.Material
import org.dam.tfg.androidapp.models.Mesa
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.util.FormulaEncryption
import java.util.UUID

/**
 * Clase que gestiona el almacenamiento de datos en memoria para la aplicación
 */
class DataStore {
    // State flows para los datos
    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials = _materials.asStateFlow()

    private val _formulas = MutableStateFlow<List<Formula>>(emptyList())
    val formulas = _formulas.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _mesas = MutableStateFlow<List<Mesa>>(emptyList())
    val mesas = _mesas.asStateFlow()

    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history = _history.asStateFlow()

    // Inicializar datos
    suspend fun initialize(): Boolean {
        return try {
            loadAllData()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Cargar todos los datos
    private suspend fun loadAllData() {
        loadMaterials()
        loadFormulas()
        loadUsers()
        loadMesas()
        loadHistory()
    }

    // Materiales
    suspend fun loadMaterials() {
        // Datos de ejemplo
        val mockMaterials = listOf(
            Material(id = "1", name = "Madera", price = 2.0),
            Material(id = "2", name = "Metal", price = 5.0),
            Material(id = "3", name = "Plástico", price = 1.5)
        )
        _materials.value = mockMaterials
    }

    suspend fun addMaterial(material: Material): Boolean {
        val updatedList = _materials.value.toMutableList()
        updatedList.add(material.copy(id = UUID.randomUUID().toString()))
        _materials.value = updatedList
        return true
    }

    suspend fun updateMaterial(material: Material): Boolean {
        val updatedList = _materials.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == material.id }
        if (index != -1) {
            updatedList[index] = material
            _materials.value = updatedList
            return true
        }
        return false
    }

    suspend fun deleteMaterial(id: String): Boolean {
        val updatedList = _materials.value.toMutableList()
        val removed = updatedList.removeIf { it.id == id }
        if (removed) {
            _materials.value = updatedList
            return true
        }
        return false
    }

    // Fórmulas
    suspend fun loadFormulas() {
        // Datos de ejemplo
        val mockFormulas = listOf(
            Formula(
                id = "1",
                name = "Cubetas",
                formula = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJmb3JtdWxhLWVuY3J5cHRpb24iLCJmb3JtdWxhIjoia1hvQk1kT2hjZU8yV09HYTlUU3dpTVA1WVc4eEp5Y1NzTmJ1UFNGYk8rLzhzU1V5RmhzMmhXdWlDMzNodmRhbyIsImV4cCI6MTc1NDI1NjI2OSwiaWF0IjoxNzQ2NDgwMjY5fQ.iPNdh0Sh38RPrAjFO9rDjkgdisDlQqiLM8vKN4AdXuk",
                formulaEncrypted = true,
                variables = mapOf("tiempoTrabajo" to "20")
            ),
            Formula(
                id = "2",
                name = "Tramos",
                formula = "precio = largo * ancho * 0.5",
                formulaEncrypted = false,
                variables = mapOf("largo" to "100", "ancho" to "50")
            )
        )
        _formulas.value = mockFormulas
    }

    suspend fun addFormula(formula: Formula): Boolean {
        val updatedList = _formulas.value.toMutableList()
        updatedList.add(formula.copy(id = UUID.randomUUID().toString()))
        _formulas.value = updatedList
        return true
    }

    suspend fun updateFormula(formula: Formula): Boolean {
        val updatedList = _formulas.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == formula.id }
        if (index != -1) {
            updatedList[index] = formula
            _formulas.value = updatedList
            return true
        }
        return false
    }

    suspend fun deleteFormula(id: String): Boolean {
        val updatedList = _formulas.value.toMutableList()
        val removed = updatedList.removeIf { it.id == id }
        if (removed) {
            _formulas.value = updatedList
            return true
        }
        return false
    }

    // Usuarios
    suspend fun loadUsers() {
        // Datos de ejemplo
        val mockUsers = listOf(
            User(
                id = "68192cac5b6def0c2447263e",
                username = "admin",
                password = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
                type = "admin"
            ),
            User(
                id = "2",
                username = "user",
                password = "04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb",
                type = "user"
            )
        )
        _users.value = mockUsers
    }

    suspend fun addUser(user: User): Boolean {
        val updatedList = _users.value.toMutableList()
        updatedList.add(user.copy(id = UUID.randomUUID().toString()))
        _users.value = updatedList
        return true
    }

    suspend fun updateUser(user: User): Boolean {
        val updatedList = _users.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == user.id }
        if (index != -1) {
            updatedList[index] = user
            _users.value = updatedList
            return true
        }
        return false
    }

    suspend fun deleteUser(id: String): Boolean {
        val updatedList = _users.value.toMutableList()
        val removed = updatedList.removeIf { it.id == id }
        if (removed) {
            _users.value = updatedList
            return true
        }
        return false
    }

    // Mesas (Presupuestos)
    suspend fun loadMesas() {
        // Datos de ejemplo
        val mockMesas = listOf(
            Mesa(
                id = "681925c95b6def0c244725ed",
                tipo = "3",
                tramos = listOf(
                    org.dam.tfg.androidapp.models.Tramo(
                        numero = 1,
                        largo = 200,
                        ancho = 200,
                        precio = 340000.0,
                        tipo = "CENTRAL"
                    ),
                    org.dam.tfg.androidapp.models.Tramo(
                        numero = 2,
                        largo = 200,
                        ancho = 200,
                        precio = 340000.0,
                        tipo = "MURAL"
                    )
                ),
                elementosGenerales = listOf(
                    org.dam.tfg.androidapp.models.ElementoSeleccionado(
                        nombre = "Cajeado columna",
                        cantidad = 2,
                        precio = 240.0
                    )
                ),
                cubetas = listOf(
                    org.dam.tfg.androidapp.models.Cubeta(
                        tipo = "Diámetro 300x180",
                        numero = 2,
                        largo = 300,
                        fondo = 300,
                        alto = 180,
                        precio = 32400025.0
                    )
                ),
                modulos = listOf(
                    org.dam.tfg.androidapp.models.Modulo(
                        nombre = "Bastidor con cajonera cuatro cajones",
                        largo = 20,
                        fondo = 20,
                        alto = 20,
                        cantidad = 1,
                        precio = 80.0
                    )
                ),
                precioTotal = 96924550.0,
                fechaCreacion = "2025-05-05T20:55:37.732Z",
                username = "Quinella"
            )
        )
        _mesas.value = mockMesas
    }

    // Historial
    suspend fun loadHistory() {
        // Datos de ejemplo
        val mockHistory = listOf(
            History(
                id = "c85571aa-4b62-459d-958a-0b6716a25804",
                userId = "Quinella",
                action = "Generación de presupuesto",
                timestamp = "2025-05-05T20:55:37.732Z",
                details = "Mesa tipo: 3, Precio: 96924550€, Fecha: 2025-05-05T20:55:37.732Z"
            ),
            History(
                id = "2",
                userId = "admin",
                action = "Inicio de sesión",
                timestamp = "2025-05-05T19:30:00.000Z",
                details = "Inicio de sesión exitoso"
            ),
            History(
                id = "3",
                userId = "admin",
                action = "Edición de material",
                timestamp = "2025-05-05T19:45:00.000Z",
                details = "Material: Madera, Precio: 2.0€"
            )
        )
        _history.value = mockHistory
    }

    // Funciones de búsqueda
    fun searchMesas(query: String): List<Mesa> {
        return mesas.value.filter { mesa ->
            mesa.username.contains(query, ignoreCase = true) ||
                    mesa.fechaCreacion?.contains(query, ignoreCase = true) == true
        }
    }

    fun searchHistory(query: String): List<History> {
        return history.value.filter { history ->
            history.userId.contains(query, ignoreCase = true) ||
                    history.action.contains(query, ignoreCase = true) ||
                    history.timestamp.contains(query, ignoreCase = true)
        }
    }

    // Encriptación/desencriptación de fórmulas
    fun encryptFormula(formula: String): String {
        return FormulaEncryption.encrypt(formula)
    }

    fun decryptFormula(encryptedFormula: String, userType: String): String {
        return if (FormulaEncryption.canViewFormula(userType)) {
            try {
                FormulaEncryption.decrypt(encryptedFormula)
            } catch (e: Exception) {
                "Error al desencriptar: ${e.message}"
            }
        } else {
            "No tienes permisos para ver esta fórmula"
        }
    }

    // Verificar credenciales de usuario
    fun checkUserCredentials(username: String, hashedPassword: String): User? {
        return users.value.find {
            it.username == username && it.password == hashedPassword
        }
    }
}
