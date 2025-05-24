package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.History
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

/**
 * Interfaz para operaciones CRUD de historial
 */
interface HistoryApiService {
    @GET("api/android/history")
    suspend fun getAllHistory(): Response<ResponseBody>

    @GET("api/android/history/{id}")
    suspend fun getHistoryById(@Path("id") id: String): Response<ResponseBody>

    @POST("api/android/history")
    suspend fun createHistory(@Body history: History): Response<ResponseBody>

    @DELETE("api/android/history/delete/{id}")
    suspend fun deleteHistory(@Path("id") id: String): Response<ResponseBody>
}
