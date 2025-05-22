package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.Formula
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para operaciones CRUD de fórmulas
 */
interface FormulaApiService {
    @GET("api/android/formulas")
    suspend fun getAllFormulas(): Response<ApiResponse<List<Formula>>>

    @GET("api/android/formulas/{id}")
    suspend fun getFormulaById(@Path("id") id: String): Response<ApiResponse<Formula>>

    @POST("api/android/formulas")
    suspend fun createFormula(@Body formula: Formula): Response<ApiResponse<Formula>>

    @PUT("api/android/formulas/{id}")
    suspend fun updateFormula(@Path("id") id: String, @Body formula: Formula): Response<ApiResponse<Formula>>

    @DELETE("api/android/formulas/{id}")
    suspend fun deleteFormula(@Path("id") id: String): Response<ApiResponse<Boolean>>
}
