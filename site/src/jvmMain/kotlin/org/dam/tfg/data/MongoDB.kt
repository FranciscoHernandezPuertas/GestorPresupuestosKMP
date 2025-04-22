package org.dam.tfg.data

import com.mongodb.client.model.Filters.and
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.varabyte.kobweb.api.data.add
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import org.dam.tfg.models.User
import org.dam.tfg.util.Constants.DATABASE_NAME
import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.repositories.FormulaRepository
import org.dam.tfg.repositories.MaterialRepository
import org.dam.tfg.repositories.MesaRepository
import org.dam.tfg.repositories.UserRepository

@InitApi
fun initMongoDB(context: InitApiContext) {
    context.data.add(MongoDB(context))
}

class MongoDB(private val context: InitApiContext) : MongoRepository {
    private val client = MongoClient.create("mongodb://localhost:27017")
    private val database = client.getDatabase(DATABASE_NAME)

    private val userCollection: MongoCollection<User> = database.getCollection("users")
    private val materialCollection: MongoCollection<Material> = database.getCollection("materials")
    private val formulaCollection: MongoCollection<Formula> = database.getCollection("formulas")
    private val mesaCollection: MongoCollection<Mesa> = database.getCollection("mesas")

    val userRepository = UserRepository(userCollection)
    val materialRepository = MaterialRepository(materialCollection)
    val formulaRepository = FormulaRepository(formulaCollection)
    val mesaRepository = MesaRepository(mesaCollection)

    override suspend fun checkUserExistence(user: User): User? {
        return try {
            userCollection.find(
                and(
                    eq(User::username.name, user.username),
                    eq(User::password.name, user.password)
                )
            ).first()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    override suspend fun checkUserId(id: String): Boolean {
        return try {
            val documentCount = userCollection.countDocuments(eq("_id", id))
            documentCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun addUser(user: User): Boolean {
        return try {
            userCollection.insertOne(user)
            true
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        return try {
            val result = userCollection.replaceOne(eq("_id", user.id), user)
            result.modifiedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun deleteUser(id: String): Boolean {
        return try {
            val result = userCollection.deleteOne(eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun getUserById(id: String): User? {
        return try {
            userCollection.find(eq("_id", id)).first()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            userCollection.find().toList()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            emptyList()
        }
    }

    override suspend fun addMaterial(material: Material): Boolean {
        return try {
            materialCollection.insertOne(material)
            true
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun updateMaterial(material: Material): Boolean {
        return try {
            val result = materialCollection.replaceOne(eq("_id", material.id), material)
            result.modifiedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun deleteMaterial(id: String): Boolean {
        return try {
            val result = materialCollection.deleteOne(eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun getMaterialById(id: String): Material? {
        return try {
            materialCollection.find(eq("_id", id)).first()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    override suspend fun getAllMaterials(): List<Material> {
        return try {
            materialCollection.find().toList()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            emptyList()
        }
    }

    override suspend fun addFormula(formula: Formula): Boolean {
        return try {
            formulaCollection.insertOne(formula)
            true
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun updateFormula(formula: Formula): Boolean {
        return try {
            val result = formulaCollection.replaceOne(eq("_id", formula.id), formula)
            result.modifiedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun deleteFormula(id: String): Boolean {
        return try {
            val result = formulaCollection.deleteOne(eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun getFormulaById(id: String): Formula? {
        return try {
            formulaCollection.find(eq("_id", id)).first()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    override suspend fun getAllFormulas(): List<Formula> {
        return try {
            formulaCollection.find().toList()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            emptyList()
        }
    }

    override suspend fun addMesa(mesa: Mesa): Boolean {
        return try {
            mesaCollection.insertOne(mesa)
            true
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun updateMesa(mesa: Mesa): Boolean {
        return try {
            val result = mesaCollection.replaceOne(eq("_id", mesa.id), mesa)
            result.modifiedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun deleteMesa(id: String): Boolean {
        return try {
            val result = mesaCollection.deleteOne(eq("_id", id))
            result.deletedCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun getMesaById(id: String): Mesa? {
        return try {
            mesaCollection.find(eq("_id", id)).first()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    override suspend fun getAllMesas(): List<Mesa> {
        return try {
            mesaCollection.find().toList()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            emptyList()
        }
    }
}