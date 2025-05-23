package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.Formula
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

/**
 * Interfaz para operaciones CRUD de fórmulas
 */
interface FormulaApiService {
    @GET("api/android/formulas/list")
    suspend fun getAllFormulas(): Response<ResponseBody>

    @GET("api/android/formulas/get/{id}")
    suspend fun getFormulaById(@Path("id") id: String): Response<ResponseBody>

    @POST("api/android/formulas/create")
    suspend fun createFormula(@Body formula: Formula): Response<ResponseBody>

    @PUT("api/android/formulas/update/{id}")
    suspend fun updateFormula(@Path("id") id: String, @Body formula: Formula): Response<ResponseBody>

    @DELETE("api/android/formulas/delete/{id}")
    suspend fun deleteFormula(@Path("id") id: String): Response<ResponseBody>
}
