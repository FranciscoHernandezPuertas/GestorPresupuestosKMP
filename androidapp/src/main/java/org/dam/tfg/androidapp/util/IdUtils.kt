package org.dam.tfg.androidapp.util

import android.util.Log
import java.util.UUID

/**
 * Utilidad para gestionar los identificadores en la base de datos
 */
object IdUtils {
    private const val TAG = "IdUtils"

    /**
     * Genera un ID único sin guiones
     */
    fun generateId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * Normaliza un ID, quitándole los guiones si es necesario
     */
    fun normalizeId(id: String): String {
        return id.replace("-", "")
    }

    /**
     * Verifica si dos IDs son equivalentes (considerando la normalización)
     */
    fun areIdsEqual(id1: String, id2: String): Boolean {
        return normalizeId(id1) == normalizeId(id2)
    }

    /**
     * Intenta extraer un ID válido de diferentes formatos posibles
     */
    fun extractValidId(rawId: Any?): String {
        if (rawId == null) return generateId()

        val idStr = when (rawId) {
            is String -> rawId
            else -> rawId.toString()
        }

        return normalizeId(idStr)
    }
}
