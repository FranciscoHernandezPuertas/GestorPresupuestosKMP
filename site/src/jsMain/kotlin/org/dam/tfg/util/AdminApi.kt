package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.models.User
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Headers

object AdminApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val token get() = localStorage.getItem("jwtToken") ?: ""

    private suspend fun fetchWithAuth(endpoint: String, method: String = "GET", body: String? = null): String {
        val headers = Headers().apply {
            append("Content-Type", "application/json")
            append("Authorization", "Bearer $token")
        }

        val init = RequestInit(
            method = method,
            headers = headers,
            body = body
        )

        // Cambiar esto para quitar el prefijo /api/
        val response = window.fetch(endpoint, init).await()

        if (!response.ok) {
            val errorText = response.text().await()
            val error = try {
                json.decodeFromString<ErrorResponse>(errorText)
            } catch (e: Exception) {
                ErrorResponse(errorText)
            }
            throw Exception(error.message)
        }

        return response.text().await()
    }

    // API para Usuarios
    suspend fun getUsers(): List<User> {
        val response = fetchWithAuth("admin/users")
        return json.decodeFromString(response)
    }

    suspend fun saveUser(user: User): User {
        val body = json.encodeToString(user)
        val response = fetchWithAuth("admin/user", "POST", body)
        return json.decodeFromString(response)
    }

    suspend fun deleteUser(id: String): Boolean {
        val body = json.encodeToString(id)
        val response = fetchWithAuth("admin/deleteuser", "POST", body)
        return json.decodeFromString(response)
    }

    // API para Materiales
    suspend fun getMaterials(): List<Material> {
        val response = fetchWithAuth("admin/materials")
        return json.decodeFromString(response)
    }

    suspend fun saveMaterial(material: Material): Material {
        val body = json.encodeToString(material)
        val response = fetchWithAuth("admin/material", "POST", body)
        return json.decodeFromString(response)
    }

    suspend fun deleteMaterial(id: String): Boolean {
        val body = json.encodeToString(id)
        val response = fetchWithAuth("admin/deletematerial", "POST", body)
        return json.decodeFromString(response)
    }

    // API para Fórmulas
    suspend fun getFormulas(): List<Formula> {
        val response = fetchWithAuth("admin/formulas")
        return json.decodeFromString(response)
    }

    suspend fun saveFormula(formula: Formula): Formula {
        val body = json.encodeToString(formula)
        val response = fetchWithAuth("admin/formula", "POST", body)
        return json.decodeFromString(response)
    }

    suspend fun deleteFormula(id: String): Boolean {
        val body = json.encodeToString(id)
        val response = fetchWithAuth("admin/deleteformula", "POST", body)
        return json.decodeFromString(response)
    }
}