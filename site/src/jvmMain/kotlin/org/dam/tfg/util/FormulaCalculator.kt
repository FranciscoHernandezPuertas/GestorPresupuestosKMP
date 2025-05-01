package org.dam.tfg.util

import net.objecthunter.exp4j.ExpressionBuilder


object FormulaCalculator {
    fun evaluateFormula(formula: String, variables: Map<String, Double>): Double {
        try {
            val expression = ExpressionBuilder(formula)
                .variables(variables.keys)
                .build()

            variables.forEach { (variable, value) ->
                expression.setVariable(variable, value)
            }

            return expression.evaluate()
        } catch (e: Exception) {
            // En caso de error, devolver 0 o manejar según sea necesario
            return 0.0
        }
    }
}