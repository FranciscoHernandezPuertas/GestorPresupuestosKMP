package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.Budget
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

/**
 * Interfaz para operaciones CRUD de presupuestos (mesas)
 */
interface BudgetApiService {
    @GET("api/android/budgets")
    suspend fun getAllBudgets(): Response<ResponseBody>

    @GET("api/android/budgets/{id}")
    suspend fun getBudgetById(@Path("id") id: String): Response<ResponseBody>

    @POST("api/android/budgets")
    suspend fun createBudget(@Body budget: Budget): Response<ResponseBody>

    @PUT("api/android/budgets/{id}")
    suspend fun updateBudget(@Path("id") id: String, @Body budget: Budget): Response<ResponseBody>

    @DELETE("api/android/budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): Response<ResponseBody>
}
