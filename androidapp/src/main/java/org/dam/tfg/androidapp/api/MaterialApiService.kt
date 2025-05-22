package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.Material
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para operaciones CRUD de materiales
 */
interface MaterialApiService {
    @GET("api/android/materials")
    suspend fun getAllMaterials(): Response<ApiResponse<List<Material>>>

    @GET("api/android/materials/{id}")
    suspend fun getMaterialById(@Path("id") id: String): Response<ApiResponse<Material>>

    @POST("api/android/materials")
    suspend fun createMaterial(@Body material: Material): Response<ApiResponse<Material>>

    @PUT("api/android/materials/{id}")
    suspend fun updateMaterial(@Path("id") id: String, @Body material: Material): Response<ApiResponse<Material>>

    @DELETE("api/android/materials/{id}")
    suspend fun deleteMaterial(@Path("id") id: String): Response<ApiResponse<Boolean>>
}
