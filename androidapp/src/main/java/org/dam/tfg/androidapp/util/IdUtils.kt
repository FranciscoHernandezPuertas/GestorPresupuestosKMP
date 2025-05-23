package org.dam.tfg.androidapp.util

import android.util.Log
import java.util.UUID

/**
 * Utilidad para gestionar los identificadores en la base de datos
 * Asegura compatibilidad entre IDs de MongoDB (ObjectId) y UUIDs
 */
object IdUtils {
    private const val TAG = "IdUtils"

    /**
     * Genera un ID único sin guiones, compatible con el formato esperado en el servidor
     */
    fun generateId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * Normaliza un ID para asegurar compatibilidad con el servidor
     * - Elimina guiones y espacios
     * - No modifica ObjectIDs de MongoDB que son hexadecimales de 24 caracteres
     */
    fun normalizeId(id: String): String {
        val trimmed = id.trim()

        // Si es un ID vacío, genera uno nuevo
        if (trimmed.isEmpty()) {
            return generateId()
        }

        // Si ya parece un ObjectId de MongoDB (24 caracteres hex), dejarlo como está
        if (trimmed.length == 24 && trimmed.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
            Log.d(TAG, "ID parece ser un ObjectId válido, manteniendo formato: $trimmed")
            return trimmed
        }

        // En otro caso, normalizar quitando guiones
        val normalized = trimmed.replace("-", "")
        Log.d(TAG, "ID normalizado: $id -> $normalized")
        return normalized
    }

    /**
     * Verifica si dos IDs son equivalentes (considerando la normalización)
     */
    fun areIdsEqual(id1: String?, id2: String?): Boolean {
        if (id1 == null && id2 == null) return true
        if (id1 == null || id2 == null) return false

        val normalizedId1 = normalizeId(id1)
        val normalizedId2 = normalizeId(id2)

        val result = normalizedId1 == normalizedId2
        if (!result) {
            Log.d(TAG, "IDs diferentes después de normalización: $normalizedId1 != $normalizedId2")
        }
        return result
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
