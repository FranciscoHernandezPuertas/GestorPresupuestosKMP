package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.Material
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

/**
 * Interfaz para operaciones CRUD de materiales
 */
interface MaterialApiService {
    @GET("api/android/materials/list")
    suspend fun getAllMaterials(): Response<ResponseBody>

    @GET("api/android/materials/get/{id}")
    suspend fun getMaterialById(@Path("id") id: String): Response<ResponseBody>

    @POST("api/android/materials/create")
    suspend fun createMaterial(@Body material: Material): Response<ResponseBody>

    @PUT("api/android/materials/update/{id}")
    suspend fun updateMaterial(@Path("id") id: String, @Body material: Material): Response<ResponseBody>

    @DELETE("api/android/materials/delete/{id}")
    suspend fun deleteMaterial(@Path("id") id: String): Response<ResponseBody>
}
