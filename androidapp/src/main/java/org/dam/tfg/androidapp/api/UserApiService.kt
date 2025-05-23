package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.User
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

/**
 * Interfaz para operaciones CRUD de usuarios
 */
interface UserApiService {
    @GET("api/android/users")
    suspend fun getAllUsers(): Response<ResponseBody>

    @GET("api/android/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ResponseBody>

    @POST("api/android/users")
    suspend fun createUser(@Body user: User): Response<ResponseBody>

    @PUT("api/android/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): Response<ResponseBody>

    @DELETE("api/android/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<ResponseBody>
}
