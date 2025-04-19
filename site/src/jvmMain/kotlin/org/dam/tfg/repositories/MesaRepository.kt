package org.dam.tfg.repositories

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.dam.tfg.models.table.Mesa

class MesaRepository(private val collection: MongoCollection<Mesa>) {

    suspend fun findAll(): List<Mesa> {
        return collection.find().toList()
    }

    suspend fun findById(id: String): Mesa? {
        return collection.find(eq("_id", id)).firstOrNull()
    }

    suspend fun findByTipo(tipo: String): List<Mesa> {
        return collection.find(eq("tipo", tipo)).toList()
    }

    suspend fun save(mesa: Mesa) {
        if (mesa.id.isNotEmpty()) {
            collection.replaceOne(eq("_id", mesa.id), mesa)
        } else {
            collection.insertOne(mesa)
        }
    }

    suspend fun delete(id: String) {
        collection.deleteOne(eq("_id", id))
    }
}