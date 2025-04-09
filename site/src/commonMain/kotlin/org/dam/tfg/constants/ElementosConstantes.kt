package org.dam.tfg.constants

import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.table.LimiteTramo

object ElementosConstantes {
    val MESAS = listOf(
        "1 tramo",
        "2 tramos",
        "3 tramos",
        "4 tramos"
    )

    val MESAS_LIMITES = mapOf(
        "1 tramo" to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        ),
        "2 tramos" to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            2 to LimiteTramo(numTramo = 2, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        ),
        "3 tramos" to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            2 to LimiteTramo(numTramo = 2, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            3 to LimiteTramo(numTramo = 3, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        ),
        "4 tramos" to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            2 to LimiteTramo(numTramo = 2, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            3 to LimiteTramo(numTramo = 3, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            4 to LimiteTramo(numTramo = 4, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        )
    )

    val ELEMENTOS_GENERALES = listOf(
        "Peto lateral",
        "Kit lavamanos pulsador",
        "Peto lateral",
        "Kit lavamanos pulsador",
        "Esquina en chaflán",
        "Kit lavam. pedal simple",
        "Esquina redondeada",
        "Kit lavam. pedal doble",
        "Cajeado columna",
        "Baquetón en seno",
        "Aro de desbarace",
        "Baqueton perimetrico"
    )

    val LIMITES_ELEMENTOS_GENERALES = mapOf(
        "Peto lateral" to ItemWithLimits(
            id = "peto_lateral",
            name = "Peto lateral",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        "Kit lavamanos pulsador" to ItemWithLimits(
            id = "kit_lavamanos_pulsador",
            name = "Kit lavamanos pulsador",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Esquina en chaflán" to ItemWithLimits(
            id = "esquina_chaflan",
            name = "Esquina en chaflán",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Kit lavam. pedal simple" to ItemWithLimits(
            id = "kit_lavamanos_pedal_simple",
            name = "Kit lavam. pedal simple",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Esquina redondeada" to ItemWithLimits(
            id = "esquina_redondeada",
            name = "Esquina redondeada",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Kit lavam. pedal doble" to ItemWithLimits(
            id = "kit_lavamanos_pedal_doble",
            name = "Kit lavam. pedal doble",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Cajeado columna" to ItemWithLimits(
            id = "cajeado_columna",
            name = "Cajeado columna",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Baquetón en seno" to ItemWithLimits(
            id = "baqueton_seno",
            name = "Baquetón en seno",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Aro de desbarace" to ItemWithLimits(
            id = "aro_desbarace",
            name = "Aro de desbarace",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        "Baqueton perimetrico" to ItemWithLimits(
            id = "baqueton_perimetrico",
            name = "Baqueton perimetrico",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        )
    )

    val TIPOS_CUBETAS = listOf(
        "Diametro 300x180",
        "Diametro 360x180",
        "Diametro 380x180",
        "Diametro 420x180",
        "Diametro 460x180",
        "Cuadrada 400x400x250",
        "Cuadrada 400x400×300",
        "Cuadrada 450x450x250",
        "Cuadrada 450x450x300",
        "Cuadrada 500×500×250",
        "Cuadrada 500x500×300",
        "Rectangular 325x300x150",
        "Rectangular 500x300x300",
        "Rectangular 500x400x250",
        "Rectangular 600x450x300",
        "Rectangular 600x500×250",
        "Rectangular 600x500x300",
        "Rectangular 600x500x320",
        "Rectangular 630x510x380",
        "Rectangular 700x450x350",
        "Rectangular 800x500x380",
        "Rectangular 955x510x380",
        "Rectangular 1280x510x380"
    )

    val LIMITES_CUBETAS = mapOf(
        "Diametro 300x180" to ItemWithLimits(
            id = "cubeta_d300",
            name = "Diametro 300x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Diametro 360x180" to ItemWithLimits(
            id = "cubeta_d360",
            name = "Diametro 360x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Diametro 380x180" to ItemWithLimits(
            id = "cubeta_d380",
            name = "Diametro 380x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Diametro 420x180" to ItemWithLimits(
            id = "cubeta_d420",
            name = "Diametro 420x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Diametro 460x180" to ItemWithLimits(
            id = "cubeta_d460",
            name = "Diametro 460x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Cuadrada 400x400x250" to ItemWithLimits(
            id = "cubeta_c400_250",
            name = "Cuadrada 400x400x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Cuadrada 400x400×300" to ItemWithLimits(
            id = "cubeta_c400_300",
            name = "Cuadrada 400x400×300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Cuadrada 450x450x250" to ItemWithLimits(
            id = "cubeta_c450_250",
            name = "Cuadrada 450x450x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Cuadrada 450x450x300" to ItemWithLimits(
            id = "cubeta_c450_300",
            name = "Cuadrada 450x450x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Cuadrada 500×500×250" to ItemWithLimits(
            id = "cubeta_c500_250",
            name = "Cuadrada 500×500×250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Cuadrada 500x500×300" to ItemWithLimits(
            id = "cubeta_c500_300",
            name = "Cuadrada 500x500×300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 325x300x150" to ItemWithLimits(
            id = "cubeta_r325",
            name = "Rectangular 325x300x150",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 500x300x300" to ItemWithLimits(
            id = "cubeta_r500_300",
            name = "Rectangular 500x300x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 500x400x250" to ItemWithLimits(
            id = "cubeta_r500_400",
            name = "Rectangular 500x400x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 600x450x300" to ItemWithLimits(
            id = "cubeta_r600_450",
            name = "Rectangular 600x450x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 600x500×250" to ItemWithLimits(
            id = "cubeta_r600_500_250",
            name = "Rectangular 600x500×250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 600x500x300" to ItemWithLimits(
            id = "cubeta_r600_500_300",
            name = "Rectangular 600x500x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 600x500x320" to ItemWithLimits(
            id = "cubeta_r600_500_320",
            name = "Rectangular 600x500x320",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 630x510x380" to ItemWithLimits(
            id = "cubeta_r630",
            name = "Rectangular 630x510x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 700x450x350" to ItemWithLimits(
            id = "cubeta_r700",
            name = "Rectangular 700x450x350",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 800x500x380" to ItemWithLimits(
            id = "cubeta_r800",
            name = "Rectangular 800x500x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 955x510x380" to ItemWithLimits(
            id = "cubeta_r955",
            name = "Rectangular 955x510x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        ),
        "Rectangular 1280x510x380" to ItemWithLimits(
            id = "cubeta_r1280",
            name = "Rectangular 1280x510x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 1
        )
    )
}