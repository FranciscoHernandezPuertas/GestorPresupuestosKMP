package org.dam.tfg.androidapp.api

import org.dam.tfg.androidapp.api.model.ApiResponse
import org.dam.tfg.androidapp.models.Budget
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para operaciones CRUD de presupuestos (mesas)
 */
interface BudgetApiService {
    @GET("api/android/budgets")
    suspend fun getAllBudgets(): Response<ApiResponse<List<Budget>>>

    @GET("api/android/budgets/{id}")
    suspend fun getBudgetById(@Path("id") id: String): Response<ApiResponse<Budget>>

    @POST("api/android/budgets")
    suspend fun createBudget(@Body budget: Budget): Response<ApiResponse<Budget>>

    @PUT("api/android/budgets/{id}")
    suspend fun updateBudget(@Path("id") id: String, @Body budget: Budget): Response<ApiResponse<Budget>>

    @DELETE("api/android/budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): Response<ApiResponse<Boolean>>
}
