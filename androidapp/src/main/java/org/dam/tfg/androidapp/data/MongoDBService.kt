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
            println("Error en authenticateUser: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // El resto del código permanece igual...

    // Users CRUD
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        val documents = usersCollection.find().toList()
        documents.forEach { document ->
            users.add(documentToUser(document))
        }
        return@withContext users
    }

    suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            val document = usersCollection.find(filter).firstOrNull()
            return@withContext document?.let { doc -> documentToUser(doc) }
        } catch (e: Exception) {
            println("Error en getUserById: ${e.message}")
            e.printStackTrace()
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
            return@withContext false
        }
    }

    // Modificar los métodos de actualización y eliminación para manejar posibles errores de conversión
    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = try {
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

            usersCollection.updateOne(filter, update)
            logAction(user.username, "Actualización de usuario", "Usuario: ${user.username}")
            return@withContext true
        } catch (e: Exception) {
            println("Error en updateUser: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun deleteUser(id: String, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            usersCollection.deleteOne(filter)
            logAction(username, "Eliminación de usuario", "ID: $id")
            return@withContext true
        } catch (e: Exception) {
            println("Error en deleteUser: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    // Materials CRUD
    suspend fun getAllMaterials(): List<Material> = withContext(Dispatchers.IO) {
        val materials = mutableListOf<Material>()
        val documents = materialsCollection.find().toList()
        documents.forEach { document ->
            materials.add(documentToMaterial(document))
        }
        return@withContext materials
    }

    suspend fun getMaterialById(id: String): Material? = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            val document = materialsCollection.find(filter).firstOrNull()
            return@withContext document?.let { doc -> documentToMaterial(doc) }
        } catch (e: Exception) {
            println("Error en getMaterialById: ${e.message}")
            e.printStackTrace()
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
            return@withContext false
        }
    }

    suspend fun updateMaterial(material: Material, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(material._id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", material._id)
            }

            val update = Document("\$set", Document()
                .append("name", material.name)
                .append("price", material.price)
            )

            materialsCollection.updateOne(filter, update)
            logAction(username, "Actualización de material", "Material: ${material.name}, Precio: ${material.price}")
            return@withContext true
        } catch (e: Exception) {
            println("Error en updateMaterial: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun deleteMaterial(id: String, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            materialsCollection.deleteOne(filter)
            logAction(username, "Eliminación de material", "ID: $id")
            return@withContext true
        } catch (e: Exception) {
            println("Error en deleteMaterial: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    // Formulas CRUD
    suspend fun getAllFormulas(): List<Formula> = withContext(Dispatchers.IO) {
        val formulas = mutableListOf<Formula>()
        val documents = formulasCollection.find().toList()
        documents.forEach { document ->
            formulas.add(documentToFormula(document))
        }
        return@withContext formulas
    }

    suspend fun getFormulaById(id: String): Formula? = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            val document = formulasCollection.find(filter).firstOrNull()
            return@withContext document?.let { doc -> documentToFormula(doc) }
        } catch (e: Exception) {
            println("Error en getFormulaById: ${e.message}")
            e.printStackTrace()
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
            Log.e("MongoDBService", "Error al crear fórmula: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun updateFormula(formula: Formula, username: String, jwtSecret: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // No necesitamos usar jwtSecret ya que FormulaEncryption maneja la encriptación
            // La fórmula ya debe venir encriptada desde EditFormulaScreen

            val filter = try {
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

            formulasCollection.updateOne(filter, update)
            logAction(username, "Actualización de fórmula", "Fórmula: ${formula.name}")
            return@withContext true
        } catch (e: Exception) {
            Log.e("MongoDBService", "Error al actualizar fórmula: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun deleteFormula(id: String, username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = try {
                Filters.eq("_id", ObjectId(id))
            } catch (e: IllegalArgumentException) {
                // Si no se puede convertir a ObjectId, buscar como String
                Filters.eq("_id", id)
            }

            formulasCollection.deleteOne(filter)
            logAction(username, "Eliminación de fórmula", "ID: $id")
            return@withContext true
        } catch (e: Exception) {
            println("Error en deleteFormula: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    // Budgets CRUD
    suspend fun getAllBudgets(): List<Budget> = withContext(Dispatchers.IO) {
        val budgets = mutableListOf<Budget>()
        val documents = budgetsCollection.find().toList()
        documents.forEach { document ->
            budgets.add(documentToBudget(document))
        }
        return@withContext budgets
    }

    suspend fun getBudgetsByUsername(username: String): List<Budget> = withContext(Dispatchers.IO) {
        val budgets = mutableListOf<Budget>()
        val documents = budgetsCollection.find(Filters.eq("username", username)).toList()
        documents.forEach { document ->
            budgets.add(documentToBudget(document))
        }
        return@withContext budgets
    }

    suspend fun getBudgetsByDateRange(startDate: String, endDate: String): List<Budget> = withContext(Dispatchers.IO) {
        val budgets = mutableListOf<Budget>()
        val filter = Filters.and(
            Filters.gte("fechaCreacion", startDate),
            Filters.lte("fechaCreacion", endDate)
        )
        val documents = budgetsCollection.find(filter).toList()
        documents.forEach { document ->
            budgets.add(documentToBudget(document))
        }
        return@withContext budgets
    }

    // History CRUD
    suspend fun getAllHistory(): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        val documents = historyCollection.find().toList()
        documents.forEach { document ->
            historyList.add(documentToHistory(document))
        }
        return@withContext historyList
    }

    suspend fun getHistoryByUsername(username: String): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        val documents = historyCollection.find(Filters.eq("userId", username)).toList()
        documents.forEach { document ->
            historyList.add(documentToHistory(document))
        }
        return@withContext historyList
    }

    suspend fun getHistoryByAction(action: String): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        val documents = historyCollection.find(Filters.regex("action", action)).toList()
        documents.forEach { document ->
            historyList.add(documentToHistory(document))
        }
        return@withContext historyList
    }

    suspend fun getHistoryByDateRange(startDate: String, endDate: String): List<History> = withContext(Dispatchers.IO) {
        val historyList = mutableListOf<History>()
        val filter = Filters.and(
            Filters.gte("timestamp", startDate),
            Filters.lte("timestamp", endDate)
        )
        val documents = historyCollection.find(filter).toList()
        documents.forEach { document ->
            historyList.add(documentToHistory(document))
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

        historyCollection.insertOne(document)
    }

    // Helper methods to convert Document to model objects
    private fun documentToUser(document: Document): User {
        // Manejo seguro del _id que puede ser String u ObjectId
        val id = when (val idValue = document.get("_id")) {
            is ObjectId -> idValue.toString()
            is String -> idValue
            else -> throw IllegalArgumentException("ID de usuario con formato inesperado: ${idValue?.javaClass?.name}")
        }

        return User(
            _id = id,
            username = document.getString("username"),
            password = document.getString("password"),
            type = document.getString("type")
        )
    }

    private fun documentToMaterial(document: Document): Material {
        // Manejo seguro del _id que puede ser String u ObjectId
        val id = when (val idValue = document.get("_id")) {
            is ObjectId -> idValue.toString()
            is String -> idValue
            else -> throw IllegalArgumentException("ID de material con formato inesperado: ${idValue?.javaClass?.name}")
        }

        // Manejo seguro del precio que puede ser Double o Integer
        val price = when (val priceValue = document.get("price")) {
            is Double -> priceValue
            is Int -> priceValue.toDouble()
            is Long -> priceValue.toDouble()
            else -> 0.0
        }

        return Material(
            _id = id,
            name = document.getString("name") ?: "",
            price = price
        )
    }

    private fun documentToFormula(document: Document): Formula {
        val variablesDoc = document.get("variables", Document::class.java)
        val variables = mutableMapOf<String, String>()

        variablesDoc.forEach { key, value ->
            variables[key] = value.toString()
        }

        // Manejo seguro del _id que puede ser String u ObjectId
        val id = when (val idValue = document.get("_id")) {
            is ObjectId -> idValue.toString()
            is String -> idValue
            else -> throw IllegalArgumentException("ID de fórmula con formato inesperado: ${idValue?.javaClass?.name}")
        }

        return Formula(
            _id = id,
            name = document.getString("name") ?: "",
            formula = document.getString("formula") ?: "",
            formulaEncrypted = document.getBoolean("formulaEncrypted", true),
            variables = variables
        )
    }

    private fun documentToHistory(document: Document): History {
        return History(
            _id = document.getString("_id"),
            userId = document.getString("userId"),
            action = document.getString("action"),
            timestamp = document.getString("timestamp"),
            details = document.getString("details")
        )
    }

    private fun documentToBudget(document: Document): Budget {
        // Manejo seguro del _id que puede ser String u ObjectId
        val id = when (val idValue = document.get("_id")) {
            is ObjectId -> idValue.toString()
            is String -> idValue
            else -> throw IllegalArgumentException("ID de presupuesto con formato inesperado: ${idValue?.javaClass?.name}")
        }

        return Budget(
            _id = id,
            tipo = document.getString("tipo") ?: "",
            tramos = parseTramos(document.getList("tramos", Document::class.java, emptyList())),
            elementosGenerales = parseElementosGenerales(document.getList("elementosGenerales", Document::class.java, emptyList())),
            cubetas = parseCubetas(document.getList("cubetas", Document::class.java, emptyList())),
            modulos = parseModulos(document.getList("modulos", Document::class.java, emptyList())),
            precioTotal = document.getLong("precioTotal") ?: 0L,
            fechaCreacion = document.getString("fechaCreacion") ?: "",
            username = document.getString("username") ?: "",
            error = document.getString("error") ?: ""
        )
    }

    private fun parseTramos(documents: List<Document>?): List<Tramo> {
        return documents?.map { doc ->
            Tramo(
                numero = doc.getInteger("numero") ?: 0,
                largo = doc.getInteger("largo") ?: 0,
                ancho = doc.getInteger("ancho") ?: 0,
                precio = doc.getLong("precio") ?: 0L,
                tipo = doc.getString("tipo") ?: "",
                error = doc.getString("error") ?: ""
            )
        } ?: emptyList()
    }

    private fun parseElementosGenerales(documents: List<Document>?): List<ElementoGeneral> {
        return documents?.map { doc ->
            ElementoGeneral(
                nombre = doc.getString("nombre") ?: "",
                cantidad = doc.getInteger("cantidad") ?: 0,
                precio = doc.getLong("precio") ?: 0L,
                limite = parseLimite(doc.get("limite", Document::class.java))
            )
        } ?: emptyList()
    }

    private fun parseLimite(document: Document?): Limite {
        return if (document != null) {
            Limite(
                id = document.getString("id") ?: "",
                name = document.getString("name") ?: "",
                minQuantity = document.getInteger("minQuantity") ?: 0,
                maxQuantity = document.getInteger("maxQuantity") ?: 0,
                initialQuantity = document.getInteger("initialQuantity") ?: 0
            )
        } else {
            Limite("", "", 0, 0, 0)
        }
    }

    private fun parseCubetas(documents: List<Document>?): List<Cubeta> {
        return documents?.map { doc ->
            Cubeta(
                tipo = doc.getString("tipo") ?: "",
                numero = doc.getInteger("numero") ?: 0,
                largo = doc.getInteger("largo") ?: 0,
                fondo = doc.getInteger("fondo") ?: 0,
                alto = doc.getInteger("alto") ?: 0,
                precio = doc.getLong("precio") ?: 0L,
                error = doc.getString("error") ?: "",
                minQuantity = doc.getInteger("minQuantity") ?: 0
            )
        } ?: emptyList()
    }

    private fun parseModulos(documents: List<Document>?): List<Modulo> {
        return documents?.map { doc ->
            Modulo(
                nombre = doc.getString("nombre") ?: "",
                largo = doc.getInteger("largo") ?: 0,
                fondo = doc.getInteger("fondo") ?: 0,
                alto = doc.getInteger("alto") ?: 0,
                cantidad = doc.getInteger("cantidad") ?: 0,
                limite = parseLimite(doc.get("limite", Document::class.java)),
                precio = doc.getLong("precio") ?: 0L
            )
        } ?: emptyList()
    }

    // Función de extensión para obtener una lista con manejo de nulos
    private fun <T> Document.getList(key: String, clazz: Class<T>, defaultValue: List<T> = emptyList()): List<T> {
        return try {
            val value = this.get(key)
            if (value == null) {
                defaultValue
            } else if (value is List<*>) {
                @Suppress("UNCHECKED_CAST")
                value as List<T>
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
}