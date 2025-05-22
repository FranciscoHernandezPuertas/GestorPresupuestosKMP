package org.dam.tfg.androidapp.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dam.tfg.androidapp.api.ApiClient
import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.*
import org.dam.tfg.androidapp.util.CryptoUtil
import org.dam.tfg.androidapp.util.IdUtils
import retrofit2.Response
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Repositorio que gestiona todas las operaciones con la API REST
 */
class ApiRepository {
    private val TAG = "ApiRepository"

    // Autenticación
    suspend fun login(username: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Intentando iniciar sesión para: $username")

                val user = User(
                    _id = "",
                    username = username,
                    password = password,  // La contraseña se enviará en texto plano a la API, que hará el hash en el servidor
                    type = "user"
                )

                val response = ApiClient.authService.login(user)

                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()?.data

                    if (authResponse != null) {
                        // Obtener el interceptor para configurar el token y el tipo de usuario
                        val interceptor = ApiClient.okHttpClient.interceptors.first() as? AuthInterceptor

                        // Configurar el token para futuras solicitudes
                        interceptor?.setToken(authResponse.token)

                        // Configurar también el tipo de usuario (importante para las operaciones con fórmulas)
                        interceptor?.setUserType(authResponse.user.type)

                        Log.d(TAG, "Inicio de sesión exitoso para: ${authResponse.user.username}, tipo: ${authResponse.user.type}")

                        // Construir y devolver el usuario
                        return@withContext User(
                            _id = authResponse.user.id,
                            username = authResponse.user.username,
                            password = "", // No almacenamos la contraseña
                            type = authResponse.user.type
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            // Intentar extraer el mensaje de error del JSON
                            val errorObject = Json.decodeFromString<ApiResponse<Any>>(errorBody)
                            errorObject.error ?: "Error desconocido"
                        } catch (e: Exception) {
                            "Error: $errorBody"
                        }
                    } else {
                        response.message() ?: "Error desconocido"
                    }

                    Log.e(TAG, "Error de inicio de sesión: Código ${response.code()}, $errorMessage")
                }

                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Excepción en login: ${e.message}", e)
                return@withContext null
            }
        }
    }

    // Usuarios
    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.userService.getAllUsers()
                handleResponse(response) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllUsers: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getUserById(id: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.userService.getUserById(id)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en getUserById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // No es necesario hashear la contraseña aquí, lo hará el servidor
                val response = ApiClient.userService.createUser(user)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en createUser: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.userService.updateUser(user._id, user)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateUser: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteUser(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.userService.deleteUser(id)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en deleteUser: ${e.message}", e)
                false
            }
        }
    }

    // Materiales
    suspend fun getAllMaterials(): List<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.materialService.getAllMaterials()
                handleResponse(response) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllMaterials: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getMaterialById(id: String): Material? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.materialService.getMaterialById(id)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en getMaterialById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createMaterial(material: Material): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.materialService.createMaterial(material)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en createMaterial: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateMaterial(material: Material): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.materialService.updateMaterial(material._id, material)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateMaterial: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteMaterial(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.materialService.deleteMaterial(id)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en deleteMaterial: ${e.message}", e)
                false
            }
        }
    }

    // Fórmulas
    suspend fun getAllFormulas(): List<Formula> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.formulaService.getAllFormulas()
                handleResponse(response) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllFormulas: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getFormulaById(id: String): Formula? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.formulaService.getFormulaById(id)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en getFormulaById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createFormula(formula: Formula): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.formulaService.createFormula(formula)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en createFormula: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateFormula(formula: Formula): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.formulaService.updateFormula(formula._id, formula)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateFormula: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteFormula(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.formulaService.deleteFormula(id)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en deleteFormula: ${e.message}", e)
                false
            }
        }
    }

    // Presupuestos (budgets)
    suspend fun getAllBudgets(): List<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.budgetService.getAllBudgets()
                handleResponse(response) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllBudgets: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getBudgetById(id: String): Budget? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.budgetService.getBudgetById(id)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en getBudgetById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createBudget(budget: Budget): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.budgetService.createBudget(budget)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en createBudget: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateBudget(budget: Budget): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.budgetService.updateBudget(budget._id, budget)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateBudget: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteBudget(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.budgetService.deleteBudget(id)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en deleteBudget: ${e.message}", e)
                false
            }
        }
    }

    // Historial
    suspend fun getAllHistory(): List<History> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.historyService.getAllHistory()
                handleResponse(response) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllHistory: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getHistoryById(id: String): History? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.historyService.getHistoryById(id)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en getHistoryById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createHistory(history: History): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.historyService.createHistory(history)
                handleResponseBoolean(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error en createHistory: ${e.message}", e)
                false
            }
        }
    }

    // Funciones auxiliares para manejar respuestas
    private fun <T> handleResponse(response: Response<ApiResponse<T>>): T? {
        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                apiResponse.data
            } else {
                Log.e(TAG, "Error en respuesta: ${apiResponse?.error}")
                null
            }
        } else {
            Log.e(TAG, "Error HTTP: ${response.code()} - ${response.message()}")
            null
        }
    }

    private fun <T> handleResponseBoolean(response: Response<ApiResponse<T>>): Boolean {
        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                true
            } else {
                Log.e(TAG, "Error en respuesta: ${apiResponse?.error}")
                false
            }
        } else {
            Log.e(TAG, "Error HTTP: ${response.code()} - ${response.message()}")
            false
        }
    }
}

/**
 * Interceptor para añadir el token de autenticación en las cabeceras
 */
class AuthInterceptor : okhttp3.Interceptor {
    private var token: String? = null
    private var userType: String? = null

    fun setToken(token: String) {
        this.token = token
    }

    fun setUserType(type: String) {
        this.userType = type
    }

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        // Iniciar con la petición original
        var requestBuilder = originalRequest.newBuilder()

        // Añadir token si está disponible
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Añadir tipo de usuario si está disponible (importante para operaciones con fórmulas)
        if (userType != null) {
            requestBuilder.addHeader("X-User-Type", userType!!)
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}
