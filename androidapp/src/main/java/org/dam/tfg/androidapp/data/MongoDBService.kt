package org.dam.tfg.androidapp.data

import android.util.Log
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.bson.Document
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.data.MongoDBConstants.DATABASE_NAME
import org.dam.tfg.androidapp.models.*
import org.dam.tfg.androidapp.util.CryptoUtil
import org.dam.tfg.androidapp.util.IdUtils
import java.util.*
import java.util.concurrent.TimeUnit
import org.dam.tfg.androidapp.BuildConfig
object MongoDBConstants {
    const val DATABASE_NAME = "gestor_db"
    const val DATABASE_URI = BuildConfig.MONGODB_URI
}

class MongoDBService(private val mongodbUri: String) {
    private val TAG = "MongoDBService"

    // Configuración del cliente con opciones mejoradas
    private val clientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(mongodbUri))
        .applyToSocketSettings { builder ->
            builder.connectTimeout(30000, TimeUnit.MILLISECONDS)
            builder.readTimeout(30000, TimeUnit.MILLISECONDS)
        }
        .applyToClusterSettings { builder ->
            builder.serverSelectionTimeout(30000, TimeUnit.MILLISECONDS)
        }
        .build()

    private val client = MongoClient.create(clientSettings)
    private val database = client.getDatabase("gestor_db")

    // Collections
    private val usersCollection = database.getCollection<Document>("users")
    private val materialsCollection = database.getCollection<Document>("materials")
    private val formulasCollection = database.getCollection<Document>("formulas")
    private val historyCollection = database.getCollection<Document>("history")
    private val budgetsCollection = database.getCollection<Document>("mesas")

    // Authentication
    suspend fun authenticateUser(username: String, password: String): User? = withContext(Dispatchers.IO) {
        val hashedPassword = CryptoUtil.hashSHA256(password)
        val query = Filters.and(
            Filters.eq("username", username),
            Filters.eq("password", hashedPassword),
            Filters.eq("type", "admin")
        )

        try {
            val document = usersCollection.find(query).firstOrNull()
            return@withContext document?.let { doc -> documentToUser(doc) }
        } catch (e: Exception) {
            Log.e(TAG, "Error en authenticateUser: ${e.message}", e)
            throw e
        }
    }

    // Users CRUD
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        try {
            val documents = usersCollection.find().toList()
            documents.forEach { document ->
                try {
                    users.add(documentToUser(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a User: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllUsers: ${e.message}", e)
        }
        return@withContext users
    }

    // Mejorar getUserById para manejar IDs normalizados y mejorar el manejo de errores
    suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        try {
            val normalizedId = IdUtils.normalizeId(id)
            val document = usersCollection.find(Filters.eq("_id", normalizedId)).firstOrNull()
            return@withContext document?.let { documentToUser(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getUserById: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun createUser(user: User): Boolean = withContext(Dispatchers.IO) {
        val document = Document()
            .append("_id", user._id.ifEmpty { IdUtils.generateId() })
            .append("username", user.username)
            .append("password", user.password)
            .append("type", user.type)

        try {
            usersCollection.insertOne(document)
            logAction(user.username, "Creación de usuario", "Usuario: ${user.username}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error en createUser: ${e.message}", e)
            return@withContext false
        }
    }

    // Mejorar updateUser para manejar IDs normalizados y mejorar el manejo de errores
    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando usuario con ID: ${user._id}")

            // Normalizar el ID para asegurar la comparación correcta
            val normalizedId = IdUtils.normalizeId(user._id)
            Log.d(TAG, "ID normalizado: $normalizedId")

            // Intentar actualizar por ID exacto primero
            var filter = Filters.eq("_id", normalizedId)

            val update = Document("\$set", Document()
                .append("username", user.username)
                .append("password", user.password)
                .append("type", user.type)
            )

            var result = usersCollection.updateOne(filter, update)

            // Si no se actualizó ningún documento, intentar con ObjectId
            if (result.modifiedCount == 0L) {
                try {
                    val objectId = ObjectId(user._id)
                    filter = Filters.eq("_id", objectId)
                    result = usersCollection.updateOne(filter, update)
                    Log.d(TAG, "Actualización por ObjectId: ${result.modifiedCount} documentos modificados")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se actualizó ningún documento, buscar manualmente
            if (result.modifiedCount == 0L) {
                try {
                    withTimeout(10000) { // Timeout más corto para la búsqueda manual
                        val allDocs = usersCollection.find().toList()
                        val docToUpdate = allDocs.find { doc ->
                            val extractedId = extractId(doc)
                            IdUtils.areIdsEqual(normalizedId, extractedId)
                        }

                        if (docToUpdate != null) {
                            // Si encontramos el documento, actualizarlo usando su _id
                            val docId = docToUpdate.get("_id")
                            filter = Filters.eq("_id", docId)
                            result = usersCollection.updateOne(filter, update)
                            Log.d(TAG, "Actualización manual: ${result.modifiedCount} documentos modificados")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en actualización manual: ${e.message}", e)
                }
            }

            Log.d(TAG, "Resultado final de actualización: ${result.modifiedCount} documentos modificados")

            if (result.modifiedCount > 0) {
                logAction(user.username, "Actualización de usuario", "Usuario: ${user.username}")
                return@withContext true
            } else {
                Log.d(TAG, "No se encontró el usuario para actualizar")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en updateUser: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun deleteUser(id: String, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Intentar eliminar por ID exacto primero
            var filter = Filters.eq("_id", id)
            var result = usersCollection.deleteOne(filter)

            // Si no se eliminó ningún documento, intentar con ObjectId
            if (result.deletedCount == 0L) {
                try {
                    val objectId = ObjectId(id)
                    filter = Filters.eq("_id", objectId)
                    result = usersCollection.deleteOne(filter)
                    Log.d(TAG, "Eliminación por ObjectId: ${result.deletedCount} documentos eliminados")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se eliminó ningún documento, intentar con formato BSON/ObjectId
            if (result.deletedCount == 0L) {
                // Para documentos con formato BSON/ObjectId, necesitamos otra estrategia
                // Primero encontrar el documento manualmente
                try {
                    val allDocs = usersCollection.find().toList()
                    val docToDelete = allDocs.find { doc ->
                        val idValue = doc.get("_id")
                        if (idValue is Document && idValue.containsKey("\$oid")) {
                            idValue.getString("\$oid") == id
                        } else {
                            false
                        }
                    }

                    if (docToDelete != null) {
                        // Si encontramos el documento, eliminarlo usando su _id
                        val docId = docToDelete.get("_id")
                        filter = Filters.eq("_id", docId)
                        result = usersCollection.deleteOne(filter)
                        Log.d(TAG, "Eliminación manual por formato BSON/ObjectId: ${result.deletedCount} documentos eliminados")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en eliminación manual: ${e.message}", e)
                }
            }

            if (result.deletedCount > 0) {
                logAction(username, "Eliminación de usuario", "ID: $id")
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en deleteUser: ${e.message}", e)
            return@withContext false
        }
    }

    // Materials CRUD
    suspend fun getAllMaterials(): List<Material> = withContext(Dispatchers.IO) {
        val materials = mutableListOf<Material>()
        try {
            val documents = materialsCollection.find().toList()
            documents.forEach { document ->
                try {
                    materials.add(documentToMaterial(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a Material: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllMaterials: ${e.message}", e)
        }
        return@withContext materials
    }

    suspend fun getMaterialById(id: String): Material? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando material con ID: $id")

            // Intentar buscar por ID exacto primero
            var document = materialsCollection.find(Filters.eq("_id", id)).firstOrNull()

            // Si no se encuentra, intentar buscar como ObjectId
            if (document == null) {
                try {
                    val objectId = ObjectId(id)
                    document = materialsCollection.find(Filters.eq("_id", objectId)).firstOrNull()
                    Log.d(TAG, "Búsqueda por ObjectId: ${document != null}")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se encuentra, intentar buscar en formato BSON/ObjectId
            if (document == null) {
                // Para documentos con formato BSON/ObjectId, necesitamos otra estrategia
                // Intentar buscar todos y filtrar manualmente
                try {
                    val allDocs = materialsCollection.find().toList()
                    document = allDocs.find { doc ->
                        val idValue = doc.get("_id")
                        if (idValue is Document && idValue.containsKey("\$oid")) {
                            idValue.getString("\$oid") == id
                        } else {
                            false
                        }
                    }
                    Log.d(TAG, "Búsqueda manual por formato BSON/ObjectId: ${document != null}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error en búsqueda manual: ${e.message}", e)
                }
            }

            if (document != null) {
                val material = documentToMaterial(document)
                Log.d(TAG, "Material encontrado: ${material.name}")
                return@withContext material
            } else {
                Log.d(TAG, "Material no encontrado")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getMaterialById: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun createMaterial(material: Material, username: String): Boolean = withContext(Dispatchers.IO) {
        val document = Document()
            .append("_id", material._id.ifEmpty { UUID.randomUUID().toString() })
            .append("name", material.name)
            .append("price", material.price)

        try {
            materialsCollection.insertOne(document)
            logAction(username, "Creación de material", "Material: ${material.name}, Precio: ${material.price}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error en createMaterial: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun updateMaterial(material: Material, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando material con ID: ${material._id}")

            // Intentar actualizar por ID exacto primero
            var filter = Filters.eq("_id", material._id)

            val update = Document("\$set", Document()
                .append("name", material.name)
                .append("price", material.price)
            )

            var result = materialsCollection.updateOne(filter, update)

            // Si no se actualizó ningún documento, intentar con ObjectId
            if (result.modifiedCount == 0L) {
                try {
                    val objectId = ObjectId(material._id)
                    filter = Filters.eq("_id", objectId)
                    result = materialsCollection.updateOne(filter, update)
                    Log.d(TAG, "Actualización por ObjectId: ${result.modifiedCount} documentos modificados")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se actualizó ningún documento, intentar con formato BSON/ObjectId
            if (result.modifiedCount == 0L) {
                // Para documentos con formato BSON/ObjectId, necesitamos otra estrategia
                // Primero encontrar el documento manualmente
                try {
                    val allDocs = materialsCollection.find().toList()
                    val docToUpdate = allDocs.find { doc ->
                        val idValue = doc.get("_id")
                        if (idValue is Document && idValue.containsKey("\$oid")) {
                            idValue.getString("\$oid") == material._id
                        } else {
                            false
                        }
                    }

                    if (docToUpdate != null) {
                        // Si encontramos el documento, actualizarlo usando su _id
                        val docId = docToUpdate.get("_id")
                        filter = Filters.eq("_id", docId)
                        result = materialsCollection.updateOne(filter, update)
                        Log.d(TAG, "Actualización manual por formato BSON/ObjectId: ${result.modifiedCount} documentos modificados")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en actualización manual: ${e.message}", e)
                }
            }

            Log.d(TAG, "Resultado final de actualización: ${result.modifiedCount} documentos modificados")

            if (result.modifiedCount > 0) {
                logAction(username, "Actualización de material", "Material: ${material.name}, Precio: ${material.price}")
                return@withContext true
            } else {
                Log.d(TAG, "No se encontró el material para actualizar")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en updateMaterial: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun deleteMaterial(id: String, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Intentar eliminar por ID exacto primero
            var filter = Filters.eq("_id", id)
            var result = materialsCollection.deleteOne(filter)

            // Si no se eliminó ningún documento, intentar con ObjectId
            if (result.deletedCount == 0L) {
                try {
                    val objectId = ObjectId(id)
                    filter = Filters.eq("_id", objectId)
                    result = materialsCollection.deleteOne(filter)
                    Log.d(TAG, "Eliminación por ObjectId: ${result.deletedCount} documentos eliminados")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se eliminó ningún documento, intentar con formato BSON/ObjectId
            if (result.deletedCount == 0L) {
                // Para documentos con formato BSON/ObjectId, necesitamos otra estrategia
                // Primero encontrar el documento manualmente
                try {
                    val allDocs = materialsCollection.find().toList()
                    val docToDelete = allDocs.find { doc ->
                        val idValue = doc.get("_id")
                        if (idValue is Document && idValue.containsKey("\$oid")) {
                            idValue.getString("\$oid") == id
                        } else {
                            false
                        }
                    }

                    if (docToDelete != null) {
                        // Si encontramos el documento, eliminarlo usando su _id
                        val docId = docToDelete.get("_id")
                        filter = Filters.eq("_id", docId)
                        result = materialsCollection.deleteOne(filter)
                        Log.d(TAG, "Eliminación manual por formato BSON/ObjectId: ${result.deletedCount} documentos eliminados")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en eliminación manual: ${e.message}", e)
                }
            }

            if (result.deletedCount > 0) {
                logAction(username, "Eliminación de material", "ID: $id")
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en deleteMaterial: ${e.message}", e)
            return@withContext false
        }
    }

    // Formulas CRUD
    suspend fun getAllFormulas(): List<Formula> = withContext(Dispatchers.IO) {
        val formulas = mutableListOf<Formula>()
        try {
            val documents = formulasCollection.find().toList()
            documents.forEach { document ->
                try {
                    formulas.add(documentToFormula(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a Formula: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllFormulas: ${e.message}", e)
        }
        return@withContext formulas
    }

    // Mejorar getFormulaById para manejar IDs normalizados y mejorar el manejo de errores
    suspend fun getFormulaById(id: String): Formula? = withContext(Dispatchers.IO) {
        try {
            val normalizedId = IdUtils.normalizeId(id)
            val document = formulasCollection.find(Filters.eq("_id", normalizedId)).firstOrNull()
            return@withContext document?.let { documentToFormula(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getFormulaById: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun createFormula(formula: Formula, username: String, jwtSecret: String): Boolean = withContext(Dispatchers.IO) {
        val document = Document()
            .append("_id", formula._id.ifEmpty { IdUtils.generateId() })
            .append("name", formula.name)
            .append("formula", formula.formula)
            .append("formulaEncrypted", formula.formulaEncrypted)
            .append("variables", Document(formula.variables))

        try {
            formulasCollection.insertOne(document)
            logAction(username, "Creación de fórmula", "Fórmula: ${formula.name}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear fórmula: ${e.message}", e)
            return@withContext false
        }
    }

    // Mejorar updateFormula para manejar IDs normalizados y mejorar el manejo de errores
    suspend fun updateFormula(formula: Formula, username: String, jwtSecret: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando fórmula con ID: ${formula._id}")

            // Normalizar el ID para asegurar la comparación correcta
            val normalizedId = IdUtils.normalizeId(formula._id)
            Log.d(TAG, "ID normalizado: $normalizedId")

            // Intentar actualizar por ID exacto primero
            var filter = Filters.eq("_id", normalizedId)

            val update = Document("\$set", Document()
                .append("name", formula.name)
                .append("formula", formula.formula)
                .append("formulaEncrypted", formula.formulaEncrypted)
                .append("variables", Document(formula.variables))
            )

            var result = formulasCollection.updateOne(filter, update)

            // Si no se actualizó ningún documento, intentar con ObjectId
            if (result.modifiedCount == 0L) {
                try {
                    val objectId = ObjectId(formula._id)
                    filter = Filters.eq("_id", objectId)
                    result = formulasCollection.updateOne(filter, update)
                    Log.d(TAG, "Actualización por ObjectId: ${result.modifiedCount} documentos modificados")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se actualizó ningún documento, buscar manualmente
            if (result.modifiedCount == 0L) {
                try {
                    withTimeout(10000) { // Timeout más corto para la búsqueda manual
                        val allDocs = formulasCollection.find().toList()
                        val docToUpdate = allDocs.find { doc ->
                            val extractedId = extractId(doc)
                            IdUtils.areIdsEqual(normalizedId, extractedId)
                        }

                        if (docToUpdate != null) {
                            // Si encontramos el documento, actualizarlo usando su _id
                            val docId = docToUpdate.get("_id")
                            filter = Filters.eq("_id", docId)
                            result = formulasCollection.updateOne(filter, update)
                            Log.d(TAG, "Actualización manual: ${result.modifiedCount} documentos modificados")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en actualización manual: ${e.message}", e)
                }
            }

            Log.d(TAG, "Resultado final de actualización: ${result.modifiedCount} documentos modificados")

            if (result.modifiedCount > 0) {
                logAction(username, "Actualización de fórmula", "Fórmula: ${formula.name}")
                return@withContext true
            } else {
                Log.d(TAG, "No se encontró la fórmula para actualizar")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar fórmula: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun deleteFormula(id: String, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Intentar eliminar por ID exacto primero
            var filter = Filters.eq("_id", id)
            var result = formulasCollection.deleteOne(filter)

            // Si no se eliminó ningún documento, intentar con ObjectId
            if (result.deletedCount == 0L) {
                try {
                    val objectId = ObjectId(id)
                    filter = Filters.eq("_id", objectId)
                    result = formulasCollection.deleteOne(filter)
                    Log.d(TAG, "Eliminación por ObjectId: ${result.deletedCount} documentos eliminados")
                } catch (e: IllegalArgumentException) {
                    Log.d(TAG, "ID no es un ObjectId válido")
                }
            }

            // Si aún no se eliminó ningún documento, intentar con formato BSON/ObjectId
            if (result.deletedCount == 0L) {
                // Para documentos con formato BSON/ObjectId, necesitamos otra estrategia
                // Primero encontrar el documento manualmente
                try {
                    val allDocs = formulasCollection.find().toList()
                    val docToDelete = allDocs.find { doc ->
                        val idValue = doc.get("_id")
                        if (idValue is Document && idValue.containsKey("\$oid")) {
                            idValue.getString("\$oid") == id
                        } else {
                            false
                        }
                    }

                    if (docToDelete != null) {
                        // Si encontramos el documento, eliminarlo usando su _id
                        val docId = docToDelete.get("_id")
                        filter = Filters.eq("_id", docId)
                        result = formulasCollection.deleteOne(filter)
                        Log.d(TAG, "Eliminación manual por formato BSON/ObjectId: ${result.deletedCount} documentos eliminados")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en eliminación manual: ${e.message}", e)
                }
            }

            if (result.deletedCount > 0) {
                logAction(username, "Eliminación de fórmula", "ID: $id")
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en deleteFormula: ${e.message}", e)
            return@withContext false
        }
    }

    // Budgets CRUD
    // Mejorar getAllBudgets para manejar timeouts y mejorar el manejo de errores
    suspend fun getAllBudgets(): List<Budget> = withContext(Dispatchers.IO) {
        try {
            val documents = budgetsCollection.find().toList()
            return@withContext documents.mapNotNull { doc ->
                try {
                    documentToBudget(doc)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a Budget: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllBudgets: ${e.message}")
            emptyList()
        }
    }

    suspend fun getBudgetsByUsername(username: String): List<Budget> = withContext(Dispatchers.IO) {
        val budgets = mutableListOf<Budget>()
        try {
            val documents = budgetsCollection.find(Filters.eq("username", username)).toList()
            documents.forEach { document ->
                try {
                    budgets.add(documentToBudget(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a Budget: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getBudgetsByUsername: ${e.message}", e)
        }
        return@withContext budgets
    }

    suspend fun getBudgetsByDateRange(startDate: String, endDate: String): List<Budget> = withContext(Dispatchers.IO) {
        val budgets = mutableListOf<Budget>()
        try {
            val filter = Filters.and(
                Filters.gte("fechaCreacion", startDate),
                Filters.lte("fechaCreacion", endDate)
            )
            val documents = budgetsCollection.find(filter).toList()
            documents.forEach { document ->
                try {
                    budgets.add(documentToBudget(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a Budget: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getBudgetsByDateRange: ${e.message}", e)
        }
        return@withContext budgets
    }

    // History CRUD
    suspend fun getAllHistory(): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        try {
            val documents = historyCollection.find().toList()
            documents.forEach { document ->
                try {
                    historyList.add(documentToHistory(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a History: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllHistory: ${e.message}", e)
        }
        return@withContext historyList
    }

    suspend fun getHistoryByUsername(username: String): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        try {
            val documents = historyCollection.find(Filters.eq("userId", username)).toList()
            documents.forEach { document ->
                try {
                    historyList.add(documentToHistory(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a History: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getHistoryByUsername: ${e.message}", e)
        }
        return@withContext historyList
    }

    suspend fun getHistoryByAction(action: String): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        try {
            val documents = historyCollection.find(Filters.regex("action", action)).toList()
            documents.forEach { document ->
                try {
                    historyList.add(documentToHistory(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a History: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getHistoryByAction: ${e.message}", e)
        }
        return@withContext historyList
    }

    suspend fun getHistoryByDateRange(startDate: String, endDate: String): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        try {
            val filter = Filters.and(
                Filters.gte("timestamp", startDate),
                Filters.lte("timestamp", endDate)
            )
            val documents = historyCollection.find(filter).toList()
            documents.forEach { document ->
                try {
                    historyList.add(documentToHistory(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a History: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getHistoryByDateRange: ${e.message}", e)
        }
        return@withContext historyList
    }

    // Logging actions
    private suspend fun logAction(userId: String, action: String, details: String) = withContext(Dispatchers.IO) {
        val document = Document()
            .append("_id", IdUtils.generateId())
            .append("userId", userId)
            .append("action", action)
            .append("timestamp", Date().toString())
            .append("details", details)

        try {
            historyCollection.insertOne(document)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar acción: ${e.message}", e)
        }
    }

    // Helper methods to convert Document to model objects
    private fun documentToUser(document: Document): User {
        return User(
            _id = document.getString("_id") ?: "", // Obligar a String
            username = document.getString("username") ?: "",
            password = document.getString("password") ?: "",
            type = document.getString("type") ?: "user"
        )
    }

    private fun documentToMaterial(document: Document): Material {
        try {
            // Extraer el ID en formato simple
            val id = extractId(document)

            // Manejo seguro del precio que puede ser Double, Integer o Long
            val price = when (val priceValue = document.get("price")) {
                is Double -> priceValue
                is Int -> priceValue.toDouble()
                is Long -> priceValue.toDouble()
                else -> {
                    Log.w(TAG, "Precio con formato inesperado: ${priceValue?.javaClass?.name}")
                    0.0
                }
            }

            return Material(
                _id = id,
                name = document.getString("name") ?: "",
                price = price
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a Material: ${e.message}", e)
            throw e
        }
    }

    private fun documentToFormula(document: Document): Formula {
        return Formula(
            _id = document.getString("_id") ?: "", // Obligar a String
            name = document.getString("name") ?: "",
            formula = document.getString("formula") ?: "",
            formulaEncrypted = document.getBoolean("formulaEncrypted", true),
            variables = (document.get("variables") as? Document)?.toMap()?.mapValues { it.value.toString() } ?: emptyMap()
        )
    }

    private fun documentToHistory(document: Document): History {
        try {
            // Extraer el ID en formato simple
            val id = extractId(document)

            return History(
                _id = id,
                userId = document.getString("userId") ?: "",
                action = document.getString("action") ?: "",
                timestamp = document.getString("timestamp") ?: "",
                details = document.getString("details") ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a History: ${e.message}", e)
            throw e
        }
    }

    private fun documentToBudget(document: Document): Budget {
        try {
            // Extraer el ID en formato simple
            val id = extractId(document)

            val budget = Budget(
                _id = id,
                tipo = document.getString("tipo") ?: "",
                tramos = parseTramos(document.get("tramos") as? List<Document>),
                elementosGenerales = parseElementosGenerales(document.get("elementosGenerales") as? List<Document>),
                cubetas = parseCubetas(document.get("cubetas") as? List<Document>),
                modulos = parseModulos(document.get("modulos") as? List<Document>),
                precioTotal = getLongValue(document, "precioTotal"),
                fechaCreacion = document.getString("fechaCreacion") ?: "",
                username = document.getString("username") ?: "",
                error = document.getString("error") ?: ""
            )

            return budget
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a Budget: ${e.message}", e)
            throw e
        }
    }

    // Mejorar la implementación de extractId para normalizar IDs consistentemente
    private fun extractId(document: Document): String {
        try {
            val rawId = when (val idValue = document.get("_id")) {
                is String -> idValue
                is ObjectId -> idValue.toHexString()
                is Document -> {
                    if (idValue.containsKey("\$oid")) {
                        idValue.getString("\$oid") ?: IdUtils.generateId()
                    } else {
                        idValue.toString().replace("[{=}]".toRegex(), "")
                    }
                }
                else -> {
                    Log.w(TAG, "ID con formato inesperado: ${idValue?.javaClass?.name}")
                    idValue?.toString() ?: IdUtils.generateId()
                }
            }

            return IdUtils.normalizeId(rawId)
        } catch (e: Exception) {
            Log.e(TAG, "Error al extraer ID: ${e.message}", e)
            return IdUtils.generateId()
        }
    }

    private fun parseTramos(documents: List<Document>?): List<Tramo> {
        if (documents == null) return emptyList()

        val tramos = mutableListOf<Tramo>()
        for (doc in documents) {
            try {
                tramos.add(
                    Tramo(
                        numero = getIntValue(doc, "numero"),
                        largo = getIntValue(doc, "largo"),
                        ancho = getIntValue(doc, "ancho"),
                        precio = getLongValue(doc, "precio"),
                        tipo = doc.getString("tipo") ?: "",
                        error = doc.getString("error") ?: ""
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear Tramo: ${e.message}", e)
            }
        }
        return tramos
    }

    private fun parseElementosGenerales(documents: List<Document>?): List<ElementoGeneral> {
        if (documents == null) return emptyList()

        val elementos = mutableListOf<ElementoGeneral>()
        for (doc in documents) {
            try {
                elementos.add(
                    ElementoGeneral(
                        nombre = doc.getString("nombre") ?: "",
                        cantidad = getIntValue(doc, "cantidad"),
                        precio = getLongValue(doc, "precio"),
                        limite = parseLimite(doc.get("limite") as? Document)
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear ElementoGeneral: ${e.message}", e)
            }
        }
        return elementos
    }

    private fun parseLimite(document: Document?): Limite {
        if (document == null) return Limite("", "", 0, 0, 0)

        try {
            return Limite(
                id = document.getString("id") ?: "",
                name = document.getString("name") ?: "",
                minQuantity = getIntValue(document, "minQuantity"),
                maxQuantity = getIntValue(document, "maxQuantity"),
                initialQuantity = getIntValue(document, "initialQuantity")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al parsear Limite: ${e.message}", e)
            return Limite("", "", 0, 0, 0)
        }
    }

    private fun parseCubetas(documents: List<Document>?): List<Cubeta> {
        if (documents == null) return emptyList()

        val cubetas = mutableListOf<Cubeta>()
        for (doc in documents) {
            try {
                cubetas.add(
                    Cubeta(
                        tipo = doc.getString("tipo") ?: "",
                        numero = getIntValue(doc, "numero"),
                        largo = getIntValue(doc, "largo"),
                        fondo = getIntValue(doc, "fondo"),
                        alto = getIntValue(doc, "alto"),
                        precio = getLongValue(doc, "precio"),
                        error = doc.getString("error") ?: "",
                        minQuantity = getIntValue(doc, "minQuantity")
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear Cubeta: ${e.message}", e)
            }
        }
        return cubetas
    }

    private fun parseModulos(documents: List<Document>?): List<Modulo> {
        if (documents == null) return emptyList()

        val modulos = mutableListOf<Modulo>()
        for (doc in documents) {
            try {
                modulos.add(
                    Modulo(
                        nombre = doc.getString("nombre") ?: "",
                        largo = getIntValue(doc, "largo"),
                        fondo = getIntValue(doc, "fondo"),
                        alto = getIntValue(doc, "alto"),
                        cantidad = getIntValue(doc, "cantidad"),
                        limite = parseLimite(doc.get("limite") as? Document),
                        precio = getLongValue(doc, "precio")
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear Modulo: ${e.message}", e)
            }
        }
        return modulos
    }

    // Métodos auxiliares para obtener valores con manejo de tipos
    private fun getIntValue(doc: Document, key: String): Int {
        return when (val value = doc.get(key)) {
            is Int -> value
            is Double -> value.toInt()
            is Long -> value.toInt()
            null -> 0
            else -> {
                try {
                    value.toString().toIntOrNull() ?: 0
                } catch (e: Exception) {
                    0
                }
            }
        }
    }

    private fun getLongValue(doc: Document, key: String): Long {
        return when (val value = doc.get(key)) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            null -> 0L
            else -> {
                try {
                    value.toString().toLongOrNull() ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
    }
}

