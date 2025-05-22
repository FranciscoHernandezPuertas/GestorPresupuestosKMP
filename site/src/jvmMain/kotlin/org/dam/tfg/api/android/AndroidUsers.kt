package org.dam.tfg.api.android

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.User
import org.dam.tfg.models.UserWithoutPassword
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.bson.types.ObjectId

// Configuración de Json para tolerancia
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Obtener todos los usuarios
 * CORREGIDO: Removida la barra inicial para que coincida con el cliente
 */
@Api(routeOverride = "android/users")
suspend fun getAllAndroidUsers(context: ApiContext) {
    try {
        println("=== GET ALL USERS ===")
        println("Method: ${context.req.method}")

        val users = context.data.getValue<MongoDB>().getAllUsers()
        println("Found ${users.size} users")

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = users
                )
            )
        )
    } catch (e: Exception) {
        println("Error getting all users: ${e.message}")
        e.printStackTrace()
        context.res.status = 500
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<List<User>>(
                    success = false,
                    error = "Error al obtener usuarios: ${e.message}"
                )
            )
        )
    }
}

/**
 * Obtener usuario por ID
 * CORREGIDO: Removida la barra inicial para que coincida con el cliente
 */
@Api(routeOverride = "android/users/{id}")
suspend fun getAndroidUserById(context: ApiContext) {
    try {
        println("=== GET USER BY ID ===")
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Requested ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de usuario no puede estar vacío")
        }

        val user = context.data.getValue<MongoDB>().getUserById(id)
            ?: throw Exception("Usuario no encontrado")

        println("User found: ${user.username}")

        context.res.setBodyText(
            json.encodeToString(
                ApiResponse(
                    success = true,
                    data = user
                )
            )
        )
    } catch (e: Exception) {
        println("Error getting user by ID: ${e.message}")
        val status = if (e.message?.contains("no encontrado") == true) 404 else 500
        context.res.status = status
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<User>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Crear un nuevo usuario
 * CORREGIDO: Removida la barra inicial para que coincida con el cliente
 */
@Api(routeOverride = "android/users")
suspend fun createAndroidUser(context: ApiContext) {
    try {
        println("=== CREATE USER ===")
        println("Method: ${context.req.method}")

        // Verificar que la petición es un POST
        if (context.req.method.toString().lowercase() != "post") {
            throw Exception("Método no permitido")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de usuario")

        println("Body received: $bodyText")

        val user = try {
            json.decodeFromString<User>(bodyText)
        } catch (e: Exception) {
            println("Error decoding user: ${e.message}")
            throw Exception("Error al decodificar datos de usuario: ${e.message}")
        }

        println("User to create: username=${user.username}, type=${user.type}")

        // Validar datos del usuario
        if (user.username.isBlank()) {
            throw Exception("El nombre de usuario no puede estar vacío")
        }

        if (user.password.isBlank()) {
            throw Exception("La contraseña no puede estar vacía")
        }

        // Crear nuevo usuario con ID generado y contraseña con hash
        val newUser = User(
            id = if (user.id.isBlank()) ObjectId().toHexString() else user.id,
            username = user.username.trim(),
            password = hashPassword(user.password),
            type = user.type
        )

        println("Creating user with ID: ${newUser.id}")

        val success = context.data.getValue<MongoDB>().addUser(newUser)

        if (success) {
            println("User created successfully")
            context.res.status = 201 // Created
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<User>(
                        success = true,
                        data = newUser.copy(password = "") // No devolver la contraseña
                    )
                )
            )
        } else {
            throw Exception("No se pudo crear el usuario")
        }
    } catch (e: Exception) {
        println("Error creating user: ${e.message}")
        e.printStackTrace()
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<User>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Actualizar un usuario existente
 * CORREGIDO: Removida la barra inicial para que coincida con el cliente
 */
@Api(routeOverride = "android/users/{id}")
suspend fun updateAndroidUser(context: ApiContext) {
    try {
        println("=== UPDATE USER ===")
        println("Method: ${context.req.method}")

        // Verificar que la petición es un PUT
        if (context.req.method.toString().lowercase() != "put") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Updating user ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de usuario no puede estar vacío")
        }

        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de usuario")

        println("Body received: $bodyText")

        val user = try {
            json.decodeFromString<User>(bodyText)
        } catch (e: Exception) {
            println("Error decoding user: ${e.message}")
            throw Exception("Error al decodificar datos de usuario: ${e.message}")
        }

        // Validar datos del usuario
        if (user.username.isBlank()) {
            throw Exception("El nombre de usuario no puede estar vacío")
        }

        // Asegurar que el ID en el path coincida con el ID en el cuerpo
        val userToUpdate = user.copy(
            id = id,
            // Solo aplicar hash si la contraseña no parece ya tener hash (longitud menor a 64 caracteres)
            password = if (user.password.length < 64 && user.password.isNotBlank()) {
                hashPassword(user.password)
            } else {
                user.password
            }
        )

        println("Updating user: username=${userToUpdate.username}, type=${userToUpdate.type}")

        val success = context.data.getValue<MongoDB>().updateUser(userToUpdate)

        if (success) {
            println("User updated successfully")
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<User>(
                        success = true,
                        data = userToUpdate.copy(password = "") // No devolver la contraseña
                    )
                )
            )
        } else {
            throw Exception("No se pudo actualizar el usuario. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        println("Error updating user: ${e.message}")
        e.printStackTrace()
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<User>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

/**
 * Eliminar un usuario
 * CORREGIDO: Removida la barra inicial para que coincida con el cliente
 */
@Api(routeOverride = "android/users/{id}")
suspend fun deleteAndroidUser(context: ApiContext) {
    try {
        println("=== DELETE USER ===")
        println("Method: ${context.req.method}")

        // Verificar que la petición es un DELETE
        if (context.req.method.toString().lowercase() != "delete") {
            throw Exception("Método no permitido")
        }

        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")
        println("Deleting user ID: $id")

        if (id.isBlank()) {
            throw Exception("ID de usuario no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteUser(id)

        if (success) {
            println("User deleted successfully")
            context.res.setBodyText(
                json.encodeToString(
                    ApiResponse<Boolean>(
                        success = true,
                        data = true
                    )
                )
            )
        } else {
            throw Exception("No se pudo eliminar el usuario. Posiblemente no existe.")
        }
    } catch (e: Exception) {
        println("Error deleting user: ${e.message}")
        e.printStackTrace()
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ApiResponse<Boolean>(
                    success = false,
                    error = e.message ?: "Error desconocido"
                )
            )
        )
    }
}

// Función para crear un hash de la contraseña usando SHA-256
private fun hashPassword(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hashBytes = messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
    val hexString = StringBuilder()

    for (byte in hashBytes) {
        hexString.append(String.format("%02x", byte))
    }

    return hexString.toString()
}