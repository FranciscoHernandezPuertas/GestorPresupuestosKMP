package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para operaciones CRUD de usuarios
 */
interface UserApiService {
    @GET("android/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<User>>>

    @GET("android/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ApiResponse<User>>

    @POST("android/users")
    suspend fun createUser(@Body user: User): Response<ApiResponse<User>>

    @PUT("android/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): Response<ApiResponse<User>>

    @DELETE("android/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<ApiResponse<Boolean>>
}
