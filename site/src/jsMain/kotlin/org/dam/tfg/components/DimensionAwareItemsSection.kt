package org.dam.tfg.components

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.table.Extra
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.util.DimensionManager

@Composable
fun <T : Extra> DimensionAwareItemsSection(
    title: String,
    description: String,
    imageSrc: String,
    items: List<T>,
    allOptions: List<ItemWithLimits>,
    onItemAdded: (String, Int) -> Unit,
    onQuantityChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onQuantityZero: (Int) -> Unit,
    itemRenderer: @Composable (T, Int, ItemWithLimits?) -> Unit,
    extractDimensions: (String) -> Pair<Double, Double>,
    mesa: Mesa,
    showWarningMessage: (String) -> Unit
) {
    // Calcular dimensiones y área
    val dimensionesDisponibles = remember(mesa) {
        DimensionManager.calcularDimensionesMinimasDisponibles(mesa)
    }

    val areaTotal = remember(mesa) {
        DimensionManager.calcularAreaTotal(mesa)
    }

    val areaOcupada = remember(items) {
        DimensionManager.calcularAreaOcupada(items)
    }

    val areaDisponible = areaTotal - areaOcupada

    // Filtrar opciones disponibles según dimensiones
    val opcionesDisponibles = remember(items, mesa, areaDisponible) {
        DimensionManager.filtrarOpcionesPorDimensiones(
            allOptions,
            items,
            dimensionesDisponibles,
            extractDimensions
        )
    }

    // Utilizar el componente ExtraItemsSection existente con la lógica personalizada
    ExtraItemsSection(
        title = title,
        description = "$description (${dimensionesDisponibles.first.toInt()}x${dimensionesDisponibles.second.toInt()}mm)",
        imageSrc = imageSrc,
        items = items,
        itemOptions = opcionesDisponibles,
        onItemAdded = { tipo, numero ->
            val dimensiones = extractDimensions(tipo)
            val area = dimensiones.first * dimensiones.second

            // Verificar espacio disponible
            if (area * numero <= areaDisponible) {
                onItemAdded(tipo, numero)
            } else {
                showWarningMessage("No hay suficiente espacio disponible para añadir este elemento.")
            }
        },
        onQuantityChanged = { index, cantidad ->
            // Validar límites de espacio
            val elemento = items[index]
            val area = (elemento.largo ?: 0.0) * (elemento.ancho ?: 0.0)
            val espacioUsadoActual = area * elemento.numero
            val espacioTotalDisponible = areaDisponible + espacioUsadoActual
            val maxDisponible = if (area > 0) (espacioTotalDisponible / area).toInt() else 0

            if (cantidad <= maxDisponible) {
                onQuantityChanged(index, cantidad)
            } else {
                showWarningMessage("No hay suficiente espacio disponible para añadir más elementos de este tipo.")
            }
        },
        onDeleteClick = onDeleteClick,
        itemRenderer = { item, index, limites ->
            // Calcular límite máximo dinámico
            val area = (item.largo ?: 0.0) * (item.ancho ?: 0.0)
            val elementosActuales = item.numero
            val espacioRestante = areaDisponible + (area * elementosActuales)
            val maxDisponible = if (area > 0) (espacioRestante / area).toInt() else 0
            val limiteDinamico = minOf(maxDisponible, limites?.maxQuantity ?: 3)

            itemRenderer(
                item,
                index,
                limites?.copy(maxQuantity = limiteDinamico)
            )
        },
        extractDimensions = extractDimensions
    )
}