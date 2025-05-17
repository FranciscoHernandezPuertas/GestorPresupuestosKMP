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
import org.bson.Document
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.data.MongoDBConstants.DATABASE_NAME
import org.dam.tfg.androidapp.models.*
import org.dam.tfg.androidapp.util.CryptoUtil
import java.util.*

object MongoDBConstants {
    const val DATABASE_NAME = "gestor_db"
    const val DATABASE_URI = "mongodb://10.0.2.2:27017"
}

class MongoDBService(private val mongodbUri: String) {
    private val TAG = "MongoDBService"
    private val client = MongoClient.create(mongodbUri)
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
                users.add(documentToUser(document))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllUsers: ${e.message}", e)
        }
        return@withContext users
    }

    suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando usuario con ID: $id")

            // Intentar primero como ObjectId
            var document: Document? = null
            try {
                val objectId = ObjectId(id)
                document = usersCollection.find(Filters.eq("_id", objectId)).firstOrNull()
                Log.d(TAG, "Búsqueda por ObjectId: ${document != null}")
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "ID no es un ObjectId válido, buscando como String")
            }

            // Si no se encontró como ObjectId, intentar como String
            if (document == null) {
                document = usersCollection.find(Filters.eq("_id", id)).firstOrNull()
                Log.d(TAG, "Búsqueda por String: ${document != null}")
            }

            if (document != null) {
                val user = documentToUser(document)
                Log.d(TAG, "Usuario encontrado: ${user.username}")
                return@withContext user
            } else {
                Log.d(TAG, "Usuario no encontrado")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getUserById: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun createUser(user: User): Boolean = withContext(Dispatchers.IO) {
        val document = Document()
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

    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando usuario con ID: ${user._id}")

            // Intentar primero como ObjectId
            var filter = try {
                Filters.eq("_id", ObjectId(user._id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", user._id)
            }

            val update = Document("\$set", Document()
                .append("username", user.username)
                .append("password", user.password)
                .append("type", user.type)
            )

            val result = usersCollection.updateOne(filter, update)
            Log.d(TAG, "Resultado de actualización: ${result.modifiedCount} documentos modificados")

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
            // Intentar primero como ObjectId
            var filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            val result = usersCollection.deleteOne(filter)
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
                materials.add(documentToMaterial(document))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllMaterials: ${e.message}", e)
        }
        return@withContext materials
    }

    suspend fun getMaterialById(id: String): Material? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando material con ID: $id")

            // Intentar primero como ObjectId
            var document: Document? = null
            try {
                val objectId = ObjectId(id)
                document = materialsCollection.find(Filters.eq("_id", objectId)).firstOrNull()
                Log.d(TAG, "Búsqueda por ObjectId: ${document != null}")
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "ID no es un ObjectId válido, buscando como String")
            }

            // Si no se encontró como ObjectId, intentar como String
            if (document == null) {
                document = materialsCollection.find(Filters.eq("_id", id)).firstOrNull()
                Log.d(TAG, "Búsqueda por String: ${document != null}")
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

            // Intentar primero como ObjectId
            var filter = try {
                Filters.eq("_id", ObjectId(material._id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", material._id)
            }

            val update = Document("\$set", Document()
                .append("name", material.name)
                .append("price", material.price)
            )

            val result = materialsCollection.updateOne(filter, update)
            Log.d(TAG, "Resultado de actualización: ${result.modifiedCount} documentos modificados")

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
            // Intentar primero como ObjectId
            var filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            val result = materialsCollection.deleteOne(filter)
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
                formulas.add(documentToFormula(document))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllFormulas: ${e.message}", e)
        }
        return@withContext formulas
    }

    suspend fun getFormulaById(id: String): Formula? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando fórmula con ID: $id")

            // Intentar primero como ObjectId
            var document: Document? = null
            try {
                val objectId = ObjectId(id)
                document = formulasCollection.find(Filters.eq("_id", objectId)).firstOrNull()
                Log.d(TAG, "Búsqueda por ObjectId: ${document != null}")
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "ID no es un ObjectId válido, buscando como String")
            }

            // Si no se encontró como ObjectId, intentar como String
            if (document == null) {
                document = formulasCollection.find(Filters.eq("_id", id)).firstOrNull()
                Log.d(TAG, "Búsqueda por String: ${document != null}")
            }

            if (document != null) {
                val formula = documentToFormula(document)
                Log.d(TAG, "Fórmula encontrada: ${formula.name}")
                return@withContext formula
            } else {
                Log.d(TAG, "Fórmula no encontrada")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getFormulaById: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun createFormula(formula: Formula, username: String, jwtSecret: String): Boolean = withContext(Dispatchers.IO) {
        // No necesitamos usar jwtSecret ya que FormulaEncryption maneja la encriptación
        // La fórmula ya debe venir encriptada desde EditFormulaScreen

        val document = Document()
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

    suspend fun updateFormula(formula: Formula, username: String, jwtSecret: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando fórmula con ID: ${formula._id}")

            // Intentar primero como ObjectId
            var filter = try {
                Filters.eq("_id", ObjectId(formula._id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", formula._id)
            }

            val update = Document("\$set", Document()
                .append("name", formula.name)
                .append("formula", formula.formula)
                .append("formulaEncrypted", formula.formulaEncrypted)
                .append("variables", Document(formula.variables))
            )

            val result = formulasCollection.updateOne(filter, update)
            Log.d(TAG, "Resultado de actualización: ${result.modifiedCount} documentos modificados")

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
            // Intentar primero como ObjectId
            var filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            val result = formulasCollection.deleteOne(filter)
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
    suspend fun getAllBudgets(): List<Budget> = withContext(Dispatchers.IO) {
        val budgets = mutableListOf<Budget>()
        try {
            val documents = budgetsCollection.find().toList()
            documents.forEach { document ->
                try {
                    budgets.add(documentToBudget(document))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a Budget: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllBudgets: ${e.message}", e)
        }
        return@withContext budgets
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
                historyList.add(documentToHistory(document))
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
                historyList.add(documentToHistory(document))
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
                historyList.add(documentToHistory(document))
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
                historyList.add(documentToHistory(document))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getHistoryByDateRange: ${e.message}", e)
        }
        return@withContext historyList
    }

    // Logging actions
    private suspend fun logAction(userId: String, action: String, details: String) = withContext(Dispatchers.IO) {
        val document = Document()
            .append("_id", UUID.randomUUID().toString())
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
        try {
            // Manejo seguro del _id que puede ser String u ObjectId
            val id = when (val idValue = document.get("_id")) {
                is ObjectId -> idValue.toString()
                is String -> idValue
                else -> {
                    Log.w(TAG, "ID de usuario con formato inesperado: ${idValue?.javaClass?.name}")
                    idValue.toString()
                }
            }

            return User(
                _id = id,
                username = document.getString("username") ?: "",
                password = document.getString("password") ?: "",
                type = document.getString("type") ?: "user"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a User: ${e.message}", e)
            throw e
        }
    }

    private fun documentToMaterial(document: Document): Material {
        try {
            // Manejo seguro del _id que puede ser String u ObjectId
            val id = when (val idValue = document.get("_id")) {
                is ObjectId -> idValue.toString()
                is String -> idValue
                else -> {
                    Log.w(TAG, "ID de material con formato inesperado: ${idValue?.javaClass?.name}")
                    idValue.toString()
                }
            }

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
        try {
            val variablesDoc = document.get("variables", Document::class.java) ?: Document()
            val variables = mutableMapOf<String, String>()

            variablesDoc.forEach { key, value ->
                variables[key] = value.toString()
            }

            // Manejo seguro del _id que puede ser String u ObjectId
            val id = when (val idValue = document.get("_id")) {
                is ObjectId -> idValue.toString()
                is String -> idValue
                else -> {
                    Log.w(TAG, "ID de fórmula con formato inesperado: ${idValue?.javaClass?.name}")
                    idValue.toString()
                }
            }

            return Formula(
                _id = id,
                name = document.getString("name") ?: "",
                formula = document.getString("formula") ?: "",
                formulaEncrypted = document.getBoolean("formulaEncrypted", true),
                variables = variables
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a Formula: ${e.message}", e)
            throw e
        }
    }

    private fun documentToHistory(document: Document): History {
        try {
            return History(
                _id = document.getString("_id") ?: "",
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
            // Manejo seguro del _id que puede ser String u ObjectId
            val id = when (val idValue = document.get("_id")) {
                is ObjectId -> idValue.toString()
                is String -> idValue
                else -> {
                    Log.w(TAG, "ID de presupuesto con formato inesperado: ${idValue?.javaClass?.name}")
                    idValue.toString()
                }
            }

            return Budget(
                _id = id,
                tipo = document.getString("tipo") ?: "",
                tramos = parseTramos(document.getList("tramos", Document::class.java)),
                elementosGenerales = parseElementosGenerales(document.getList("elementosGenerales", Document::class.java)),
                cubetas = parseCubetas(document.getList("cubetas", Document::class.java)),
                modulos = parseModulos(document.getList("modulos", Document::class.java)),
                precioTotal = document.getLong("precioTotal") ?: 0L,
                fechaCreacion = document.getString("fechaCreacion") ?: "",
                username = document.getString("username") ?: "",
                error = document.getString("error") ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a Budget: ${e.message}", e)
            throw e
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
                        limite = parseLimite(doc.get("limite", Document::class.java))
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
                        limite = parseLimite(doc.get("limite", Document::class.java)),
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
            else -> 0
        }
    }

    private fun getLongValue(doc: Document, key: String): Long {
        return when (val value = doc.get(key)) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            else -> 0L
        }
    }

    // Método de extensión para obtener una lista con manejo de nulos
    private fun <T> Document.getList(key: String, clazz: Class<T>): List<T>? {
        return try {
            val value = this.get(key)
            if (value == null) {
                null
            } else if (value is List<*>) {
                @Suppress("UNCHECKED_CAST")
                value as List<T>
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener lista para clave $key: ${e.message}", e)
            null
        }
    }
}
