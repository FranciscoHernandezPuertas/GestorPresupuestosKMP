package org.dam.tfg.repositories

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.dam.tfg.models.User

class UserRepository(private val collection: MongoCollection<User>) {

    suspend fun findAll(): List<User> {
        return collection.find().toList()
    }

    suspend fun findById(id: String): User? {
        return collection.find(eq("_id", id)).firstOrNull()
    }

    suspend fun findByUsername(username: String): User? {
        return collection.find(eq("username", username)).firstOrNull()
    }

    suspend fun save(user: User): User {
        val existingUser = if (user.id.isNotEmpty()) {
            findById(user.id)
        } else {
            findByUsername(user.username)
        }

        if (existingUser != null && existingUser.id != user.id) {
            throw Exception("El nombre de usuario ya existe")
        }

        if (user.id.isNotEmpty()) {
            collection.replaceOne(eq("_id", user.id), user)
        } else {
            collection.insertOne(user)
        }
        return user
    }

    suspend fun delete(id: String) {
        collection.deleteOne(eq("_id", id))
    }
}