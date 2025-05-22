package org.dam.tfg.androidapp.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Cliente API centralizado para comunicarse con el servidor Kobweb
 */
object ApiClient {
    // Base URL del servidor Kobweb
    private const val BASE_URL = "https://generadorpresupuestos.onrender.com/"
    // También podemos configurar una URL alternativa para desarrollo local
    private const val LOCAL_URL = "http://10.0.2.2:8080/" // Dirección localhost para el emulador Android

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

    fun setToken(token: String) {
        this.token = token
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        // Si no hay token, proceder sin modificar la request
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // Añadir token a las cabeceras
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}
