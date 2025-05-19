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
}
