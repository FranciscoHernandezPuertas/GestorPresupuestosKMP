package org.dam.tfg.util

import com.varabyte.kobweb.browser.api
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.AuthResponse
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.Formula
import org.dam.tfg.models.History
import org.dam.tfg.models.Material
import org.dam.tfg.models.TokenValidationRequest
import org.dam.tfg.models.User
import org.dam.tfg.models.UserWithoutPassword
import org.dam.tfg.models.table.Mesa
import org.w3c.dom.get
import org.w3c.dom.set

suspend fun loginUser(user: User): UserWithoutPassword? {
    return try {
        val result = window.api.tryPost(
            apiPath = "usercheck",
            body = Json.encodeToString(user).encodeToByteArray()
        )

        result?.decodeToString()?.let { jsonResponse ->
            try {
                // Intentar parsear como respuesta exitosa
                val authResponse = Json.decodeFromString<AuthResponse>(jsonResponse)

                // Guardar datos en localStorage
                localStorage["jwt_token"] = authResponse.token
                localStorage["userType"] = authResponse.user.type
                localStorage["userId"] = authResponse.user.id
                localStorage["username"] = authResponse.user.username
                localStorage["remember"] = "true"

                authResponse.user
            } catch (e: Exception) {
                // Puede ser un error
                try {
                    val errorResponse = Json.decodeFromString<ErrorResponse>(jsonResponse)
                    console.error("Error de autenticación: ${errorResponse.message}")
                    null
                } catch (e: Exception) {
                    console.error("Error al parsear respuesta: $jsonResponse")
                    null
                }
            }
        }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

// Alias para mantener compatibilidad con código existente
suspend fun checkUserExistence(user: User): UserWithoutPassword? {
    return loginUser(user)
}

suspend fun validateToken(): UserWithoutPassword? {
    return try {
        val token = localStorage["jwt_token"] ?: return null

        val result = window.api.tryPost(
            apiPath = "validatetoken",
            body = Json.encodeToString(TokenValidationRequest(token)).encodeToByteArray()
        )

        result?.decodeToString()?.let { jsonResponse ->
            try {
                Json.decodeFromString<UserWithoutPassword>(jsonResponse)
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

suspend fun checkUserId(id: String): Boolean {
    return try {
        val result = window.api.tryPost(
            apiPath = "checkuserid",
            body = Json.encodeToString(id).encodeToByteArray()
        )

        result?.decodeToString()?.let { Json.decodeFromString<Boolean>(it) } ?: false
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

fun logout() {
    // Limpiar datos de autenticación
    localStorage.removeItem("jwt_token")
    localStorage.removeItem("userId")
    localStorage.removeItem("username")
    localStorage.removeItem("userType")
    localStorage["remember"] = "false"
}

suspend fun addUser(user: User): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/addUser",
            body = Json.encodeToString(user).encodeToByteArray()
        )?.decodeToString().toBoolean()
    }
    catch (e: Exception) {
        println(e.message.toString())
        false
    }
}

suspend fun updateUser(user: User): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/updateUser",
            body = Json.encodeToString(user).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun deleteUser(id: String): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/deleteUser",
            body = Json.encodeToString(id).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun getUserById(id: String): User? {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getUserById?id=$id"
        )

        result?.decodeToString()?.let { jsonResponse ->
            try {
                Json.decodeFromString<User>(jsonResponse)
            } catch (e: Exception) {
                console.error("Error al obtener usuario: $jsonResponse")
                null
            }
        }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

suspend fun getAllUsers(): List<User> {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getAllUsers"
        )

        result?.decodeToString()?.let { jsonResponse ->
            try {
                Json.decodeFromString<List<User>>(jsonResponse)
            } catch (e: Exception) {
                console.error("Error al obtener usuarios: $jsonResponse")
                emptyList()
            }
        } ?: emptyList()
    } catch (e: Exception) {
        console.error(e.message)
        emptyList()
    }
}

suspend fun addMaterial(material: Material): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/addMaterial",
            body = Json.encodeToString(material).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun updateMaterial(material: Material): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/updateMaterial",
            body = Json.encodeToString(material).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun deleteMaterial(id: String): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/deleteMaterial",
            body = Json.encodeToString(id).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun getMaterialById(id: String): Material? {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getMaterialById?id=$id"
        )
        result?.decodeToString()?.let { Json.decodeFromString<Material>(it) }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

// En ApiFunctions.kt
suspend fun getAllMaterials(): List<Material> {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getAllMaterials" // Agrega el prefijo "admin/"
        )
        result?.decodeToString()?.let { Json.decodeFromString<List<Material>>(it) } ?: emptyList()
    } catch (e: Exception) {
        console.error(e.message)
        emptyList()
    }
}

// FÓRMULA - CRUD
suspend fun addFormula(formula: Formula): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/addFormula",
            body = Json.encodeToString(formula).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun updateFormula(formula: Formula): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/updateFormula",
            body = Json.encodeToString(formula).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun deleteFormula(id: String): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/deleteFormula",
            body = id.encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error("Error al eliminar fórmula: ${e.message}")
        false
    }
}

suspend fun getAllFormulas(userType: String = "user"): List<Formula> {
    return try {
        // Codificar el parámetro userType para evitar problemas con caracteres especiales
        val encodedUserType = js("encodeURIComponent")(userType) as String

        val result = window.api.tryGet(
            apiPath = "admin/getAllFormulas?userType=$encodedUserType"
        )

        if (result != null) {
            val jsonString = result.decodeToString()
            try {
                Json.decodeFromString<List<Formula>>(jsonString)
            } catch (e: Exception) {
                console.error("Error al decodificar fórmulas: ${e.message}")
                console.error("Response recibida: $jsonString")
                emptyList()
            }
        } else {
            console.error("No se recibió respuesta del servidor")
            emptyList()
        }
    } catch (e: Exception) {
        console.error("Error al obtener fórmulas: ${e.message}")
        emptyList()
    }
}

suspend fun getFormulaById(id: String, userType: String = "user"): Formula? {
    return try {
        val encodedUserType = js("encodeURIComponent")(userType) as String

        val result = window.api.tryGet(
            apiPath = "admin/getFormulaById?id=$id&userType=$encodedUserType"
        )

        result?.decodeToString()?.let { Json.decodeFromString<Formula>(it) }
    } catch (e: Exception) {
        console.error("Error al obtener fórmula: ${e.message}")
        null
    }
}

// MESA - CRUD
suspend fun addMesa(mesa: Mesa): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/addMesa",
            body = Json.encodeToString(mesa).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun updateMesa(mesa: Mesa): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/updateMesa",
            body = Json.encodeToString(mesa).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun deleteMesa(id: String): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/deleteMesa",
            body = Json.encodeToString(id).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun getMesaById(id: String): Mesa? {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getMesaById?id=$id"
        )
        result?.decodeToString()?.let { Json.decodeFromString<Mesa>(it) }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

suspend fun getAllMesas(): List<Mesa> {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getAllMesas"
        )
        result?.decodeToString()?.let { Json.decodeFromString<List<Mesa>>(it) } ?: emptyList()
    } catch (e: Exception) {
        console.error(e.message)
        emptyList()
    }
}

// HISTORY - CRUD
suspend fun addHistory(history: History): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/addHistory",
            body = Json.encodeToString(history).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun updateHistory(history: History): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/updateHistory",
            body = Json.encodeToString(history).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun deleteHistory(id: String): Boolean {
    return try {
        window.api.tryPost(
            apiPath = "admin/deleteHistory",
            body = Json.encodeToString(id).encodeToByteArray()
        )?.decodeToString().toBoolean()
    } catch (e: Exception) {
        console.error(e.message.toString())
        false
    }
}

suspend fun getHistoryById(id: String): History? {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getHistoryById?id=$id"
        )
        result?.decodeToString()?.let { Json.decodeFromString<History>(it) }
    } catch (e: Exception) {
        console.error(e.message)
        null
    }
}

suspend fun getAllHistory(): List<History> {
    return try {
        val result = window.api.tryGet(
            apiPath = "admin/getAllHistory"
        )
        result?.decodeToString()?.let { Json.decodeFromString<List<History>>(it) } ?: emptyList()
    } catch (e: Exception) {
        console.error(e.message)
        emptyList()
    }
}