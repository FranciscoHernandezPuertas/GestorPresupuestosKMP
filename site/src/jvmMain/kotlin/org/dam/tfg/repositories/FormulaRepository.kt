package org.dam.tfg.repositories

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.dam.tfg.models.Formula

class FormulaRepository(private val collection: MongoCollection<Formula>) {

    suspend fun findAll(): List<Formula> {
        return collection.find().toList()
    }

    suspend fun findById(id: String): Formula? {
        return collection.find(eq("_id", id)).firstOrNull()
    }

    suspend fun findByAplicaA(aplicaA: String): List<Formula> {
        return collection.find(eq("aplicaA", aplicaA)).toList()
    }

    suspend fun save(formula: Formula) {
        if (formula.id.isNotEmpty()) {
            collection.replaceOne(eq("_id", formula.id), formula)
        } else {
            collection.insertOne(formula)
        }
    }

    suspend fun delete(id: String) {
        collection.deleteOne(eq("_id", id))
    }
}