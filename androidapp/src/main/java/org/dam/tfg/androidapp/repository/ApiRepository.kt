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
import okhttp3.ResponseBody
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Repositorio que gestiona todas las operaciones con la API REST
 */
class ApiRepository {
    private val TAG = "ApiRepository"
    private val gson = Gson()
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

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
                Log.d(TAG, "Obteniendo todos los usuarios...")
                val response = ApiClient.userService.getAllUsers()
                Log.d(TAG, "Respuesta de getAllUsers: ${response.isSuccessful}, código: ${response.code()}")

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al obtener usuarios: ${response.code()} - ${response.message()}")
                    return@withContext emptyList()
                }

                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e(TAG, "Cuerpo de respuesta nulo al obtener usuarios")
                    return@withContext emptyList()
                }

                val jsonString = responseBody.string()
                Log.d(TAG, "Respuesta para usuarios: $jsonString")

                val apiResponse = gson.fromJson<ApiResponse<List<User>>>(
                    jsonString,
                    object : TypeToken<ApiResponse<List<User>>>() {}.type
                )

                if (!apiResponse.success) {
                    Log.e(TAG, "Error en respuesta de API: ${apiResponse.error}")
                    return@withContext emptyList()
                }

                val users = apiResponse.data
                if (users == null) {
                    Log.d(TAG, "Lista de usuarios nula")
                    return@withContext emptyList()
                }

                Log.d(TAG, "Usuarios obtenidos correctamente: ${users.size}")
                return@withContext users
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllUsers: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    suspend fun getUserById(id: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Obteniendo usuario con ID: $id")
                val response = ApiClient.userService.getUserById(id)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error al obtener usuario: Código ${response.code()}")
                    return@withContext null
                }

                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e(TAG, "Cuerpo de respuesta nulo al obtener usuario")
                    return@withContext null
                }

                val jsonString = responseBody.string()
                Log.d(TAG, "Respuesta para usuario: $jsonString")

                val apiResponse = gson.fromJson<ApiResponse<User>>(
                    jsonString,
                    object : TypeToken<ApiResponse<User>>() {}.type
                )

                if (!apiResponse.success) {
                    Log.e(TAG, "Error en respuesta de API: ${apiResponse.error}")
                    return@withContext null
                }

                val user = apiResponse.data
                if (user == null) {
                    Log.e(TAG, "Usuario no encontrado en respuesta")
                    return@withContext null
                }

                Log.d(TAG, "Usuario obtenido correctamente: ${user.username}")
                return@withContext user
            } catch (e: Exception) {
                Log.e(TAG, "Error en getUserById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creando usuario: ${user.username}")
                val response = ApiClient.userService.createUser(user)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al crear usuario: ${response.code()} - ${response.message()}")
                    return@withContext false
                }

                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en createUser: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Usar getActualId para obtener el ID correcto sea _id o id
                val actualId = user.getActualId()
                Log.d(TAG, "Actualizando usuario con ID: $actualId")

                val response = ApiClient.userService.updateUser(actualId, user)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al actualizar usuario: ${response.code()} - ${response.message()}")
                    return@withContext false
                }

                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateUser: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteUser(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Eliminando usuario con ID: $id")
                val response = ApiClient.userService.deleteUser(id)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al eliminar usuario: ${response.code()} - ${response.message()}")
                    return@withContext false
                }

                return@withContext handleResponseBodyBoolean(response.body())
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
                Log.d(TAG, "getAllMaterials response: ${response.isSuccessful}")
                return@withContext handleResponseBody<List<Material>>(response.body()) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllMaterials: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getMaterialById(id: String): Material? {
        return withContext(Dispatchers.IO) {
            try {
                val normalizedId = IdUtils.normalizeId(id)
                Log.d(TAG, "Obteniendo material con ID normalizado: $normalizedId (original: $id)")
                val response = ApiClient.materialService.getMaterialById(normalizedId)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error al obtener material: Código ${response.code()}")
                    return@withContext null
                }

                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e(TAG, "Cuerpo de respuesta nulo al obtener material")
                    return@withContext null
                }

                val jsonString = responseBody.string()
                Log.d(TAG, "Respuesta para material: $jsonString")

                val apiResponse = gson.fromJson<ApiResponse<Material>>(
                    jsonString,
                    object : TypeToken<ApiResponse<Material>>() {}.type
                )

                if (!apiResponse.success) {
                    Log.e(TAG, "Error en respuesta de API: ${apiResponse.error}")
                    return@withContext null
                }

                val material = apiResponse.data
                if (material == null) {
                    Log.e(TAG, "Material no encontrado en respuesta")
                    return@withContext null
                }

                Log.d(TAG, "Material obtenido correctamente: ${material.name}, ID: ${material._id}")
                return@withContext material
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
                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en createMaterial: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateMaterial(material: Material): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Usar getActualId para obtener el ID correcto sea _id o id
                val actualId = material.getActualId()
                val response = ApiClient.materialService.updateMaterial(actualId, material)
                return@withContext handleResponseBodyBoolean(response.body())
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
                return@withContext handleResponseBodyBoolean(response.body())
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
                Log.d(TAG, "getAllFormulas: Iniciando petición")
                val response = ApiClient.formulaService.getAllFormulas()
                Log.d(TAG, "getAllFormulas response: ${response.isSuccessful}")

                return@withContext handleResponseBody<List<Formula>>(response.body()) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error en getAllFormulas: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getFormulaById(id: String): Formula? {
        return withContext(Dispatchers.IO) {
            try {
                val normalizedId = IdUtils.normalizeId(id)
                Log.d(TAG, "Obteniendo fórmula con ID normalizado: $normalizedId (original: $id)")
                val response = ApiClient.formulaService.getFormulaById(normalizedId)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error al obtener fórmula: Código ${response.code()}")
                    return@withContext null
                }

                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e(TAG, "Cuerpo de respuesta nulo al obtener fórmula")
                    return@withContext null
                }

                val jsonString = responseBody.string()
                Log.d(TAG, "Respuesta para fórmula: $jsonString")

                val apiResponse = gson.fromJson<ApiResponse<Formula>>(
                    jsonString,
                    object : TypeToken<ApiResponse<Formula>>() {}.type
                )

                if (!apiResponse.success) {
                    Log.e(TAG, "Error en respuesta de API: ${apiResponse.error}")
                    return@withContext null
                }

                val formula = apiResponse.data
                if (formula == null) {
                    Log.e(TAG, "Fórmula no encontrada en respuesta")
                    return@withContext null
                }

                Log.d(TAG, "Fórmula obtenida correctamente: ${formula.name}, ID: ${formula._id}")
                return@withContext formula
            } catch (e: Exception) {
                Log.e(TAG, "Error en getFormulaById: ${e.message}", e)
                null
            }
        }
    }

    suspend fun createFormula(formula: Formula): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creando fórmula: ${formula.name}")
                val response = ApiClient.formulaService.createFormula(formula)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al crear fórmula: ${response.code()} - ${response.message()}")
                    return@withContext false
                }

                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en createFormula: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateFormula(formula: Formula): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Usar getActualId para obtener el ID correcto sea _id o id
                val actualId = formula.getActualId()
                Log.d(TAG, "Actualizando fórmula con ID: $actualId")

                val response = ApiClient.formulaService.updateFormula(actualId, formula)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al actualizar fórmula: ${response.code()} - ${response.message()}")
                    return@withContext false
                }

                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateFormula: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteFormula(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Eliminando fórmula con ID: $id")
                val response = ApiClient.formulaService.deleteFormula(id)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Error HTTP al eliminar fórmula: ${response.code()} - ${response.message()}")
                    return@withContext false
                }

                return@withContext handleResponseBodyBoolean(response.body())
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
                Log.d(TAG, "getAllBudgets response: ${response.isSuccessful}")
                return@withContext handleResponseBody<List<Budget>>(response.body()) ?: emptyList()
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
                Log.d(TAG, "getBudgetById response: ${response.isSuccessful}")
                return@withContext handleResponseBody<Budget>(response.body())
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
                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en createBudget: ${e.message}", e)
                false
            }
        }
    }

    suspend fun updateBudget(budget: Budget): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Usar getActualId para obtener el ID correcto sea _id o id
                val actualId = budget.getActualId()
                val response = ApiClient.budgetService.updateBudget(actualId, budget)
                return@withContext handleResponseBodyBoolean(response.body())
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
                return@withContext handleResponseBodyBoolean(response.body())
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
                Log.d(TAG, "getAllHistory response: ${response.isSuccessful}")
                return@withContext handleResponseBody<List<History>>(response.body()) ?: emptyList()
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
                return@withContext handleResponseBody<History>(response.body())
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
                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en createHistory: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteHistory(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Eliminando historial con ID: $id")
                val response = ApiClient.historyService.deleteHistory(id)
                return@withContext handleResponseBodyBoolean(response.body())
            } catch (e: Exception) {
                Log.e(TAG, "Error en deleteHistory: ${e.message}", e)
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

    // Nuevas funciones para manejar respuestas de tipo ResponseBody directamente
    private inline fun <reified T> handleResponseBody(responseBody: ResponseBody?): T? {
        return try {
            if (responseBody == null) {
                Log.d(TAG, "handleResponseBody: responseBody es null")
                return handleEmptyResponse<T>()
            }

            val jsonString = responseBody.string()
            Log.d(TAG, "Respuesta recibida: ${jsonString.take(200)}${if (jsonString.length > 200) "..." else ""}")

            val apiResponse = try {
                gson.fromJson<ApiResponse<T>>(
                    jsonString,
                    object : TypeToken<ApiResponse<T>>() {}.type
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al deserializar ApiResponse: ${e.message}", e)
                // Intento alternativo de parsear el JSON directamente
                try {
                    val data = gson.fromJson<T>(jsonString, object : TypeToken<T>() {}.type)
                    return data
                } catch (innerE: Exception) {
                    Log.e(TAG, "Error al deserializar directamente: ${innerE.message}", innerE)
                    return handleEmptyResponse<T>()
                }
            }

            if (apiResponse.success) {
                val data = apiResponse.data
                if (data == null) {
                    Log.d(TAG, "API respuesta exitosa pero con datos nulos")
                    handleEmptyResponse<T>()
                } else {
                    data
                }
            } else {
                Log.e(TAG, "Error en respuesta: ${apiResponse.error}")
                handleEmptyResponse<T>()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar respuesta: ${e.message}", e)
            handleEmptyResponse<T>()
        }
    }

    private inline fun <reified T> handleEmptyResponse(): T? {
        return when {
            // Para listas, devolver lista vacía en lugar de null
            List::class.java.isAssignableFrom(T::class.java) -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    emptyList<Any>() as T
                } catch (e: Exception) {
                    Log.e(TAG, "Error al crear lista vacía: ${e.message}", e)
                    null
                }
            }
            // Para otros casos, devolver null
            else -> null
        }
    }

    private fun handleResponseBodyBoolean(responseBody: ResponseBody?): Boolean {
        return try {
            if (responseBody == null) return false

            val jsonString = responseBody.string()
            Log.d(TAG, "Respuesta recibida (boolean): ${jsonString.take(200)}${if (jsonString.length > 200) "..." else ""}")

            val apiResponse = gson.fromJson<ApiResponse<Any>>(
                jsonString,
                object : TypeToken<ApiResponse<Any>>() {}.type
            )

            if (apiResponse.success) {
                true
            } else {
                Log.e(TAG, "Error en respuesta: ${apiResponse.error}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar respuesta booleana: ${e.message}", e)
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
