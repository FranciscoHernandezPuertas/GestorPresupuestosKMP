package org.dam.tfg.androidapp.data

/**
 * Clase que gestiona el almacenamiento de datos en memoria para la aplicación
 * Esta clase es un adaptador para la clase DataStore
 */
class AppDataStore {
    private val dataStore = DataStore()

    // Propiedades
    val materials = dataStore.materials
    val formulas = dataStore.formulas
    val users = dataStore.users
    val mesas = dataStore.mesas
    val history = dataStore.history

    // Inicialización
    suspend fun initializeRealm(appId: String, username: String, password: String): Boolean {
        return dataStore.initialize()
    }

    // Métodos para materiales
    suspend fun loadMaterials() = dataStore.loadMaterials()
    suspend fun addMaterial(material: org.dam.tfg.androidapp.models.Material) = dataStore.addMaterial(material)
    suspend fun updateMaterial(material: org.dam.tfg.androidapp.models.Material) = dataStore.updateMaterial(material)
    suspend fun deleteMaterial(id: String) = dataStore.deleteMaterial(id)

    // Métodos para fórmulas
    suspend fun loadFormulas() = dataStore.loadFormulas()
    suspend fun addFormula(formula: org.dam.tfg.androidapp.models.Formula) = dataStore.addFormula(formula)
    suspend fun updateFormula(formula: org.dam.tfg.androidapp.models.Formula) = dataStore.updateFormula(formula)
    suspend fun deleteFormula(id: String) = dataStore.deleteFormula(id)

    // Métodos para usuarios
    suspend fun loadUsers() = dataStore.loadUsers()
    suspend fun addUser(user: org.dam.tfg.androidapp.models.User) = dataStore.addUser(user)
    suspend fun updateUser(user: org.dam.tfg.androidapp.models.User) = dataStore.updateUser(user)
    suspend fun deleteUser(id: String) = dataStore.deleteUser(id)

    // Métodos para mesas
    suspend fun loadMesas() = dataStore.loadMesas()

    // Métodos para historial
    suspend fun loadHistory() = dataStore.loadHistory()

    // Métodos de búsqueda
    fun searchMesas(query: String) = dataStore.searchMesas(query)
    fun searchHistory(query: String) = dataStore.searchHistory(query)

    // Métodos para encriptación/desencriptación de fórmulas
    fun encryptFormula(formula: String) = dataStore.encryptFormula(formula)
    fun decryptFormula(encryptedFormula: String, userType: String) = dataStore.decryptFormula(encryptedFormula, userType)

    // Verificación de credenciales
    fun checkUserCredentials(username: String, hashedPassword: String) = dataStore.checkUserCredentials(username, hashedPassword)

    // Cierre de conexión
    fun closeRealm() {
        // No es necesario hacer nada
    }
}
