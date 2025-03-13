package org.dam.tfg.data

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.varabyte.kobweb.api.data.add
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import org.dam.tfg.models.User
import org.dam.tfg.util.Constants.DATABASE_NAME
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.first

@InitApi
fun initMongoDB(context: InitApiContext) {
    context.data.add(MongoDB(context))
}

class MongoDB(private val context: InitApiContext) {
    private val client = MongoClient.create("mongodb://localhost:27017")
    private val database = client.getDatabase(DATABASE_NAME)
    private val userCollection: MongoCollection<User> = database.getCollection("users")

    suspend fun checkUserExistence(user: User): User? {
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
}