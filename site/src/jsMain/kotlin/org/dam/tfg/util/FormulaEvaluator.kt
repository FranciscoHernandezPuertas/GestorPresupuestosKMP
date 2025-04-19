package org.dam.tfg.util

import kotlin.js.JsExport

@JsExport
class FormulaEvaluator {
    companion object {
        fun evaluar(formulaStr: String, variables: Map<String, Double>): Double {
            try {
                // Sustituir variables
                var expresion = formulaStr
                for ((key, value) in variables) {
                    expresion = expresion.replace(key, value.toString())
                }

                // Aquí iría un evaluador real de expresiones matemáticas
                // Esta implementación es simplificada
                return 0.0
            } catch (e: Exception) {
                return 0.0
            }
        }
    }
}