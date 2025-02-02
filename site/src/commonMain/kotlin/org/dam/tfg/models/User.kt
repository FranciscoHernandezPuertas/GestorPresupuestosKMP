package org.dam.tfg.models

expect class User {
    val id: String
    val username: String
    val password: String
}

expect class UserWithoutPassword {
    val id: String
    val username: String
}