package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.History
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para operaciones CRUD de historial
 */
interface HistoryApiService {
    @GET("android/history")
    suspend fun getAllHistory(): Response<ApiResponse<List<History>>>

    @GET("android/history/{id}")
    suspend fun getHistoryById(@Path("id") id: String): Response<ApiResponse<History>>

    @POST("android/history")
    suspend fun createHistory(@Body history: History): Response<ApiResponse<History>>
}
