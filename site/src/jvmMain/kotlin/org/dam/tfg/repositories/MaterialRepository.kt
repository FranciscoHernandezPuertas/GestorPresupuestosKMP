package org.dam.tfg.repositories

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.dam.tfg.models.Material

class MaterialRepository(private val collection: MongoCollection<Material>) {

    suspend fun findAll(): List<Material> {
        return collection.find().toList()
    }

    suspend fun findById(id: String): Material? {
        return collection.find(eq("_id", id)).firstOrNull()
    }

    suspend fun save(material: Material) {
        if (material.id.isNotEmpty()) {
            collection.replaceOne(eq("_id", material.id), material)
        } else {
            collection.insertOne(material)
        }
    }

    suspend fun delete(id: String) {
        collection.deleteOne(eq("_id", id))
    }
}