package org.dam.tfg.androidapp.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import org.dam.tfg.androidapp.BuildConfig

/**
 * Cliente API centralizado para comunicarse con el servidor Kobweb
 */
object ApiClient {
    // URL alternativa para desarrollo local (emulador Android)
    private const val LOCAL_URL = "http://10.0.2.2:27017/"
    // Base URL del servidor Kobweb, obtenida desde BuildConfig
    // Si está vacía o mal configurada, se usa la URL alternativa
    private val BASE_URL = if (BuildConfig.BASE_URL.isNotBlank() && BuildConfig.BASE_URL.startsWith("http"))
        BuildConfig.BASE_URL else LOCAL_URL

    // Configuración del cliente JSON
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    // Configuración del cliente HTTP con interceptores
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Cliente Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // Servicios API
    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val userService: UserApiService = retrofit.create(UserApiService::class.java)
    val materialService: MaterialApiService = retrofit.create(MaterialApiService::class.java)
    val formulaService: FormulaApiService = retrofit.create(FormulaApiService::class.java)
    val budgetService: BudgetApiService = retrofit.create(BudgetApiService::class.java)
    val historyService: HistoryApiService = retrofit.create(HistoryApiService::class.java)
}

/**
 * Interceptor para añadir el token de autenticación en las cabeceras
 */
class AuthInterceptor : Interceptor {
    private var token: String? = null
    private var userType: String? = null

    fun setToken(token: String) {
        this.token = token
    }

    fun setUserType(type: String) {
        this.userType = type
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
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
