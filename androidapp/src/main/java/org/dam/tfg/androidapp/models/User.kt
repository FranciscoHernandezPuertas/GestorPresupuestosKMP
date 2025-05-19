package org.dam.tfg.androidapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.dam.tfg.androidapp.util.IdUtils

@Parcelize
@Serializable
data class User(
    val _id: String = IdUtils.generateId(),
    val username: String,
    val password: String, // SHA-256 hash
    val type: String
) : Parcelable
