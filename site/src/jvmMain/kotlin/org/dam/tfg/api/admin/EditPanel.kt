package org.dam.tfg.api.admin

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.User
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.util.JwtManager
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

@Api(routeOverride = "admin/users")
suspend fun getUsers(context: ApiContext) {
    try {
        // Verificar que sea un administrador
        val token = context.req.headers["Authorization"]?.toString()?.removePrefix("Bearer ")
        val user = token?.let { JwtManager.verifyToken(it) }
        if (user == null || user.type != "admin") {
            context.res.status = 403
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Acceso denegado")))
            return
        }

        val users = context.data.getValue<MongoDB>().userRepository.findAll()
        context.res.setBodyText(Json.encodeToString(users))
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(Json.encodeToString(ErrorResponse(e.message ?: "Error desconocido")))
    }
}

@Api(routeOverride = "admin/user")
suspend fun saveUser(context: ApiContext) {
    try {
        // Verificar que sea un administrador
        val token = context.req.headers["Authorization"]?.toString()?.removePrefix("Bearer ")
        val adminUser = token?.let { JwtManager.verifyToken(it) }
        if (adminUser == null || adminUser.type != "admin") {
            context.res.status = 403
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Acceso denegado")))
            return
        }

        val userRequest = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<User>(it)
        }

        if (userRequest == null) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Datos inválidos")))
            return
        }

        // Hash de la contraseña si es nueva o modificada
        val hashedUser = if (userRequest.id.isEmpty() || userRequest.password != "") {
            userRequest.copy(password = hashPassword(userRequest.password))
        } else {
            // Si es una edición sin cambiar la contraseña, obtener la original
            val existingUser = context.data.getValue<MongoDB>().userRepository.findById(userRequest.id)
            userRequest.copy(password = existingUser?.password ?: "")
        }

        context.data.getValue<MongoDB>().userRepository.save(hashedUser)
        context.res.setBodyText(Json.encodeToString(hashedUser.copy(password = "")))
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(Json.encodeToString(ErrorResponse(e.message ?: "Error desconocido")))
    }
}

@Api(routeOverride = "admin/deleteuser")
suspend fun deleteUser(context: ApiContext) {
    try {
        // Verificar que sea un administrador
        val token = context.req.headers["Authorization"]?.toString()?.removePrefix("Bearer ")
        val user = token?.let { JwtManager.verifyToken(it) }
        if (user == null || user.type != "admin") {
            context.res.status = 403
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Acceso denegado")))
            return
        }

        val userId = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<String>(it)
        }

        if (userId == null) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("ID inválido")))
            return
        }

        // No permitir eliminar al usuario actual
        if (userId == user.id) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("No puede eliminar su propio usuario")))
            return
        }

        context.data.getValue<MongoDB>().userRepository.delete(userId)
        context.res.setBodyText(Json.encodeToString(true))
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(Json.encodeToString(ErrorResponse(e.message ?: "Error desconocido")))
    }
}

private fun hashPassword(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hashBytes = messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
    val hexString = StringBuffer()

    for(byte in hashBytes) {
        hexString.append(String.format("%02x", byte))
    }
    return hexString.toString()
}

// API para Materiales
@Api(routeOverride = "admin/materials")
suspend fun getMaterials(context: ApiContext) {
    validateAdminAndExecute(context) {
        val materials = context.data.getValue<MongoDB>().materialRepository.findAll()
        context.res.setBodyText(Json.encodeToString(materials))
    }
}

@Api(routeOverride = "admin/material")
suspend fun saveMaterial(context: ApiContext) {
    validateAdminAndExecute(context) {
        val materialRequest = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<Material>(it)
        }

        if (materialRequest == null) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Datos inválidos")))
            return@validateAdminAndExecute
        }

        context.data.getValue<MongoDB>().materialRepository.save(materialRequest)
        context.res.setBodyText(Json.encodeToString(materialRequest))
    }
}

@Api(routeOverride = "admin/deletematerial")
suspend fun deleteMaterial(context: ApiContext) {
    validateAdminAndExecute(context) {
        val materialId = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<String>(it)
        }

        if (materialId == null) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("ID inválido")))
            return@validateAdminAndExecute
        }

        context.data.getValue<MongoDB>().materialRepository.delete(materialId)
        context.res.setBodyText(Json.encodeToString(true))
    }
}

// API para Fórmulas
@Api(routeOverride = "admin/formulas")
suspend fun getFormulas(context: ApiContext) {
    validateAdminAndExecute(context) {
        val formulas = context.data.getValue<MongoDB>().formulaRepository.findAll()
        context.res.setBodyText(Json.encodeToString(formulas))
    }
}

@Api(routeOverride = "admin/formula")
suspend fun saveFormula(context: ApiContext) {
    validateAdminAndExecute(context) {
        val formulaRequest = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<Formula>(it)
        }

        if (formulaRequest == null) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Datos inválidos")))
            return@validateAdminAndExecute
        }

        context.data.getValue<MongoDB>().formulaRepository.save(formulaRequest)
        context.res.setBodyText(Json.encodeToString(formulaRequest))
    }
}

@Api(routeOverride = "admin/deleteformula")
suspend fun deleteFormula(context: ApiContext) {
    validateAdminAndExecute(context) {
        val formulaId = context.req.body?.decodeToString()?.let {
            Json.decodeFromString<String>(it)
        }

        if (formulaId == null) {
            context.res.status = 400
            context.res.setBodyText(Json.encodeToString(ErrorResponse("ID inválido")))
            return@validateAdminAndExecute
        }

        context.data.getValue<MongoDB>().formulaRepository.delete(formulaId)
        context.res.setBodyText(Json.encodeToString(true))
    }
}

// Función auxiliar para validar administrador
private suspend fun validateAdminAndExecute(context: ApiContext, action: suspend () -> Unit) {
    try {
        val token = context.req.headers["Authorization"]?.toString()?.removePrefix("Bearer ")
        val user = token?.let { JwtManager.verifyToken(it) }
        if (user == null || user.type != "admin") {
            context.res.status = 403
            context.res.setBodyText(Json.encodeToString(ErrorResponse("Acceso denegado")))
            return
        }

        action()
    } catch (e: Exception) {
        context.res.status = 500
        context.res.setBodyText(Json.encodeToString(ErrorResponse(e.message ?: "Error desconocido")))
    }
}