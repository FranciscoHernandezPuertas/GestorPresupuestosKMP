package org.dam.tfg.api.admin

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.data.getValue

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.bson.types.ObjectId
import org.dam.tfg.data.MongoDB
import org.dam.tfg.models.User
import org.dam.tfg.models.ErrorResponse
import org.dam.tfg.models.Formula
import org.dam.tfg.models.History
import org.dam.tfg.models.Material
import org.dam.tfg.models.table.Mesa
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

// Configuración común para JSON
private val json = Json { ignoreUnknownKeys = true }

// USUARIOS - CRUD

@Api(routeOverride = "addUser")
suspend fun addUser(context: ApiContext) {
    try {
        // Validar que hay datos en el cuerpo
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de usuario")

        // Deserializar el usuario
        val user = try {
            json.decodeFromString<User>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de usuario: ${e.message}")
        }

        // Validar datos del usuario
        if (user.username.isBlank()) {
            throw Exception("El nombre de usuario no puede estar vacío")
        }

        if (user.password.isBlank()) {
            throw Exception("La contraseña no puede estar vacía")
        }

        // Crear nuevo usuario con datos seguros
        val newUser = user.copy(
            id = ObjectId().toHexString(),
            username = user.username.trim(),
            password = MessageDigest.getInstance("SHA-256")
                .digest(user.password.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) },
            type = user.type
        )

        // Intentar agregar el usuario a la base de datos
        val success = context.data.getValue<MongoDB>().addUser(newUser)

        // Devolver resultado
        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400 // Bad Request
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "updateUser")
suspend fun updateUser(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de usuario")

        val user = try {
            json.decodeFromString<User>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de usuario: ${e.message}")
        }

        // Validaciones
        if (user.id.isBlank()) {
            throw Exception("ID de usuario no proporcionado")
        }

        if (user.username.isBlank()) {
            throw Exception("El nombre de usuario no puede estar vacío")
        }

        // Crear el usuario actualizado
        val updatedUser = user.copy(
            password = if (user.password.length < 64) {
                // Solo aplicar hash si la contraseña aún no tiene hash
                MessageDigest.getInstance("SHA-256")
                    .digest(user.password.toByteArray(StandardCharsets.UTF_8))
                    .joinToString("") { "%02x".format(it) }
            } else {
                user.password
            }
        )

        val success = context.data.getValue<MongoDB>().updateUser(updatedUser)
        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al actualizar usuario: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "deleteUser")
suspend fun deleteUser(context: ApiContext) {
    try {
        val id = context.req.body?.decodeToString()
            ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de usuario no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteUser(id)

        if (!success) {
            throw Exception("No se pudo eliminar el usuario. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al eliminar usuario: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getUserById")
suspend fun getUserById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de usuario no puede estar vacío")
        }

        val user = context.data.getValue<MongoDB>().getUserById(id)
            ?: throw Exception("Usuario no encontrado")

        context.res.setBodyText(json.encodeToString(user))
    } catch (e: Exception) {
        context.res.status = 404 // Not Found
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener usuario: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getAllUsers")
suspend fun getAllUsers(context: ApiContext) {
    try {
        val users = context.data.getValue<MongoDB>().getAllUsers()
        context.res.setBodyText(json.encodeToString(users))
    } catch (e: Exception) {
        context.res.status = 500 // Internal Server Error
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener usuarios: ${e.message}")
            )
        )
    }
}

// MATERIALES - CRUD

@Api(routeOverride = "addMaterial")
suspend fun addMaterial(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de material")

        val material = try {
            json.decodeFromString<Material>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de material: ${e.message}")
        }

        // Validaciones
        if (material.name.isBlank()) {
            throw Exception("El nombre del material no puede estar vacío")
        }

        if (material.price < 0) {
            throw Exception("El precio no puede ser negativo")
        }

        val success = context.data.getValue<MongoDB>().addMaterial(material)
        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al agregar material: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "updateMaterial")
suspend fun updateMaterial(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de material")

        val material = try {
            json.decodeFromString<Material>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de material: ${e.message}")
        }

        // Validaciones
        if (material.id.isBlank()) {
            throw Exception("ID de material no proporcionado")
        }

        if (material.name.isBlank()) {
            throw Exception("El nombre del material no puede estar vacío")
        }

        if (material.price < 0) {
            throw Exception("El precio no puede ser negativo")
        }

        val success = context.data.getValue<MongoDB>().updateMaterial(material)

        if (!success) {
            throw Exception("No se pudo actualizar el material. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al actualizar material: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "deleteMaterial")
suspend fun deleteMaterial(context: ApiContext) {
    try {
        val id = context.req.body?.decodeToString()
            ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteMaterial(id)

        if (!success) {
            throw Exception("No se pudo eliminar el material. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al eliminar material: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getMaterialById")
suspend fun getMaterialById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de material no puede estar vacío")
        }

        val material = context.data.getValue<MongoDB>().getMaterialById(id)
            ?: throw Exception("Material no encontrado")

        context.res.setBodyText(json.encodeToString(material))
    } catch (e: Exception) {
        context.res.status = 404 // Not Found
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener material: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getAllMaterials")
suspend fun getAllMaterials(context: ApiContext) {
    try {
        val materials = context.data.getValue<MongoDB>().getAllMaterials()
        context.res.setBodyText(json.encodeToString(materials))
    } catch (e: Exception) {
        context.res.status = 500 // Internal Server Error
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener materiales: ${e.message}")
            )
        )
    }
}

// FÓRMULAS - CRUD

@Api(routeOverride = "addFormula")
suspend fun addFormula(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de fórmula")

        val formula = try {
            json.decodeFromString<Formula>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de fórmula: ${e.message}")
        }

        // Validaciones básicas
        if (formula.name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formula.formula.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        val success = context.data.getValue<MongoDB>().addFormula(formula)
        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al agregar fórmula: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "updateFormula")
suspend fun updateFormula(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de fórmula")

        val formula = try {
            json.decodeFromString<Formula>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de fórmula: ${e.message}")
        }

        // Validaciones
        if (formula.id.isBlank()) {
            throw Exception("ID de fórmula no proporcionado")
        }

        if (formula.name.isBlank()) {
            throw Exception("El nombre de la fórmula no puede estar vacío")
        }

        if (formula.formula.isBlank()) {
            throw Exception("La fórmula no puede estar vacía")
        }

        val success = context.data.getValue<MongoDB>().updateFormula(formula)

        if (!success) {
            throw Exception("No se pudo actualizar la fórmula. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al actualizar fórmula: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "deleteFormula")
suspend fun deleteFormula(context: ApiContext) {
    try {
        val id = context.req.body?.decodeToString()
            ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteFormula(id)

        if (!success) {
            throw Exception("No se pudo eliminar la fórmula. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al eliminar fórmula: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getFormulaById")
suspend fun getFormulaById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de fórmula no puede estar vacío")
        }

        val formula = context.data.getValue<MongoDB>().getFormulaById(id)
            ?: throw Exception("Fórmula no encontrada")

        context.res.setBodyText(json.encodeToString(formula))
    } catch (e: Exception) {
        context.res.status = 404 // Not Found
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener fórmula: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getAllFormulas")
suspend fun getAllFormulas(context: ApiContext) {
    try {
        val formulas = context.data.getValue<MongoDB>().getAllFormulas()
        context.res.setBodyText(json.encodeToString(formulas))
    } catch (e: Exception) {
        context.res.status = 500 // Internal Server Error
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener fórmulas: ${e.message}")
            )
        )
    }
}

// MESAS - CRUD

@Api(routeOverride = "addMesa")
suspend fun addMesa(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de mesa")

        val mesa = try {
            json.decodeFromString<Mesa>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de mesa: ${e.message}")
        }

        // Validar mesa
        if (!mesa.isValid()) {
            throw Exception(mesa.error)
        }

        val success = context.data.getValue<MongoDB>().addMesa(mesa)
        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al agregar mesa: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "updateMesa")
suspend fun updateMesa(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de mesa")

        val mesa = try {
            json.decodeFromString<Mesa>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de mesa: ${e.message}")
        }

        // Validaciones
        if (mesa.id.isBlank()) {
            throw Exception("ID de mesa no proporcionado")
        }

        // Validar mesa
        if (!mesa.isValid()) {
            throw Exception(mesa.error)
        }

        val success = context.data.getValue<MongoDB>().updateMesa(mesa)

        if (!success) {
            throw Exception("No se pudo actualizar la mesa. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al actualizar mesa: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "deleteMesa")
suspend fun deleteMesa(context: ApiContext) {
    try {
        val id = context.req.body?.decodeToString()
            ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de mesa no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteMesa(id)

        if (!success) {
            throw Exception("No se pudo eliminar la mesa. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al eliminar mesa: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getMesaById")
suspend fun getMesaById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de mesa no puede estar vacío")
        }

        val mesa = context.data.getValue<MongoDB>().getMesaById(id)
            ?: throw Exception("Mesa no encontrada")

        context.res.setBodyText(json.encodeToString(mesa))
    } catch (e: Exception) {
        context.res.status = 404 // Not Found
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener mesa: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getAllMesas")
suspend fun getAllMesas(context: ApiContext) {
    try {
        val mesas = context.data.getValue<MongoDB>().getAllMesas()
        context.res.setBodyText(json.encodeToString(mesas))
    } catch (e: Exception) {
        context.res.status = 500 // Internal Server Error
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener mesas: ${e.message}")
            )
        )
    }
}

// HISTORIAL - CRUD

@Api(routeOverride = "addHistory")
suspend fun addHistory(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de historial")

        val history = try {
            json.decodeFromString<History>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de historial: ${e.message}")
        }

        // Validaciones
        if (!history.isValid()) {
            throw Exception("Datos de historial inválidos")
        }

        val success = context.data.getValue<MongoDB>().addHistory(history)
        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al agregar historial: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "updateHistory")
suspend fun updateHistory(context: ApiContext) {
    try {
        val bodyText = context.req.body?.decodeToString()
            ?: throw Exception("No se proporcionaron datos de historial")

        val history = try {
            json.decodeFromString<History>(bodyText)
        } catch (e: Exception) {
            throw Exception("Error al decodificar datos de historial: ${e.message}")
        }

        // Validaciones
        if (history.id.isBlank()) {
            throw Exception("ID de historial no proporcionado")
        }

        if (!history.isValid()) {
            throw Exception("Datos de historial inválidos")
        }

        val success = context.data.getValue<MongoDB>().updateHistory(history)

        if (!success) {
            throw Exception("No se pudo actualizar el historial. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al actualizar historial: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "deleteHistory")
suspend fun deleteHistory(context: ApiContext) {
    try {
        val id = context.req.body?.decodeToString()
            ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de historial no puede estar vacío")
        }

        val success = context.data.getValue<MongoDB>().deleteHistory(id)

        if (!success) {
            throw Exception("No se pudo eliminar el historial. Posiblemente no existe.")
        }

        context.res.setBodyText(success.toString())
    } catch (e: Exception) {
        context.res.status = 400
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al eliminar historial: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getHistoryById")
suspend fun getHistoryById(context: ApiContext) {
    try {
        val id = context.req.params["id"] ?: throw Exception("ID no proporcionado")

        if (id.isBlank()) {
            throw Exception("ID de historial no puede estar vacío")
        }

        val history = context.data.getValue<MongoDB>().getHistoryById(id)
            ?: throw Exception("Historial no encontrado")

        context.res.setBodyText(json.encodeToString(history))
    } catch (e: Exception) {
        context.res.status = 404 // Not Found
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener historial: ${e.message}")
            )
        )
    }
}

@Api(routeOverride = "getAllHistory")
suspend fun getAllHistory(context: ApiContext) {
    try {
        val history = context.data.getValue<MongoDB>().getAllHistory()
        context.res.setBodyText(json.encodeToString(history))
    } catch (e: Exception) {
        context.res.status = 500 // Internal Server Error
        context.res.setBodyText(
            json.encodeToString(
                ErrorResponse("Error al obtener historiales: ${e.message}")
            )
        )
    }
}