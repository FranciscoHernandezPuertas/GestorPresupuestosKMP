package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.AuthResponse
import org.dam.tfg.androidapp.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz para operaciones de autenticación
 */
interface AuthApiService {
    @POST("api/android/auth/login")
    suspend fun login(@Body user: User): Response<ApiResponse<AuthResponse>>
}
