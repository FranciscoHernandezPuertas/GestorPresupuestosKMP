package org.dam.tfg.models.table

import org.dam.tfg.models.ItemWithLimits

object ElementosConstantesLimites {

    val MESAS_LIMITES = mapOf(
        MesasTipos.TRAMOS_1 to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        ),
        MesasTipos.TRAMOS_2 to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            2 to LimiteTramo(numTramo = 2, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        ),
        MesasTipos.TRAMOS_3 to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            2 to LimiteTramo(numTramo = 2, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            3 to LimiteTramo(numTramo = 3, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        ),
        MesasTipos.TRAMOS_4 to mapOf(
            1 to LimiteTramo(numTramo = 1, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            2 to LimiteTramo(numTramo = 2, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            3 to LimiteTramo(numTramo = 3, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0),
            4 to LimiteTramo(numTramo = 4, minLargo = 200.0, maxLargo = 6000.0, minAncho = 200.0, maxAncho = 6000.0)
        )
    )

    val LIMITES_ELEMENTOS_GENERALES = mapOf(
        MesasElementosGenerales.PETO_LATERAL to ItemWithLimits(
            id = "peto_lateral",
            name = "Peto lateral",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasElementosGenerales.KIT_LAVAMANOS_PULSADOR to ItemWithLimits(
            id = "kit_lavamanos_pulsador",
            name = "Kit lavamanos pulsador",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.ESQUINA_CHAFLAN to ItemWithLimits(
            id = "esquina_chaflan",
            name = "Esquina en chaflán",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.KIT_LAVAMANOS_PEDAL_SIMPLE to ItemWithLimits(
            id = "kit_lavamanos_pedal_simple",
            name = "Kit lavam. pedal simple",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.ESQUINA_REDONDEADA to ItemWithLimits(
            id = "esquina_redondeada",
            name = "Esquina redondeada",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.KIT_LAVAMANOS_PEDAL_DOBLE to ItemWithLimits(
            id = "kit_lavamanos_pedal_doble",
            name = "Kit lavam. pedal doble",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.CAJEADO_COLUMNA to ItemWithLimits(
            id = "cajeado_columna",
            name = "Cajeado columna",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.BAQUETON_SENO to ItemWithLimits(
            id = "baqueton_seno",
            name = "Baquetón en seno",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.ARO_DESBARACE to ItemWithLimits(
            id = "aro_desbarace",
            name = "Aro de desbarace",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        ),
        MesasElementosGenerales.BAQUETON_PERIMETRICO to ItemWithLimits(
            id = "baqueton_perimetrico",
            name = "Baqueton perimetrico",
            minQuantity = 0,
            maxQuantity = 5,
            initialQuantity = 0
        )
    )

    val LIMITES_CUBETAS = mapOf(
        MesasCubetas.DIAMETRO_300X180 to ItemWithLimits(
            id = "cubeta_d300",
            name = "Diametro 300x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.DIAMETRO_360X180 to ItemWithLimits(
            id = "cubeta_d360",
            name = "Diametro 360x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.DIAMETRO_380X180 to ItemWithLimits(
            id = "cubeta_d380",
            name = "Diametro 380x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.DIAMETRO_420X180 to ItemWithLimits(
            id = "cubeta_d420",
            name = "Diametro 420x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.DIAMETRO_460X180 to ItemWithLimits(
            id = "cubeta_d460",
            name = "Diametro 460x180",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.CUADRADA_400X400X250 to ItemWithLimits(
            id = "cubeta_c400_250",
            name = "Cuadrada 400x400x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.CUADRADA_400X400X300 to ItemWithLimits(
            id = "cubeta_c400_300",
            name = "Cuadrada 400x400x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.CUADRADA_450X450X250 to ItemWithLimits(
            id = "cubeta_c450_250",
            name = "Cuadrada 450x450x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.CUADRADA_450X450X300 to ItemWithLimits(
            id = "cubeta_c450_300",
            name = "Cuadrada 450x450x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.CUADRADA_500X500X250 to ItemWithLimits(
            id = "cubeta_c500_250",
            name = "Cuadrada 500x500x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.CUADRADA_500X500X300 to ItemWithLimits(
            id = "cubeta_c500_300",
            name = "Cuadrada 500x500x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_325X300X150 to ItemWithLimits(
            id = "cubeta_r325",
            name = "Rectangular 325x300x150",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_500X300X300 to ItemWithLimits(
            id = "cubeta_r500_300",
            name = "Rectangular 500x300x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_500X400X250 to ItemWithLimits(
            id = "cubeta_r500_400",
            name = "Rectangular 500x400x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_600X450X300 to ItemWithLimits(
            id = "cubeta_r600_450",
            name = "Rectangular 600x450x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_600X500X250 to ItemWithLimits(
            id = "cubeta_r600_500_250",
            name = "Rectangular 600x500x250",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_600X500X300 to ItemWithLimits(
            id = "cubeta_r600_500_300",
            name = "Rectangular 600x500x300",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_600X500X320 to ItemWithLimits(
            id = "cubeta_r600_500_320",
            name = "Rectangular 600x500x320",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_630X510X380 to ItemWithLimits(
            id = "cubeta_r630",
            name = "Rectangular 630x510x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_700X450X350 to ItemWithLimits(
            id = "cubeta_r700",
            name = "Rectangular 700x450x350",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_800X500X380 to ItemWithLimits(
            id = "cubeta_r800",
            name = "Rectangular 800x500x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_955X510X380 to ItemWithLimits(
            id = "cubeta_r955",
            name = "Rectangular 955x510x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasCubetas.RECTANGULAR_1280X510X380 to ItemWithLimits(
            id = "cubeta_r1280",
            name = "Rectangular 1280x510x380",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        )
    )

    // Reemplazando la lista MODULOS por una función que devuelve los valores de la enum
    val MODULOS = MesasModulos.values().map { it.displayName }

    val LIMITES_MODULOS = mapOf(
        MesasModulos.BASTIDOR_SIN_ESTANTE to ItemWithLimits(
            id = "bastidor_sin_estante",
            name = "Bastidor sin estante",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_ESTANTE to ItemWithLimits(
            id = "bastidor_con_estante",
            name = "Bastidor con estante",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_DOS_ESTANTES to ItemWithLimits(
            id = "bastidor_con_dos_estantes",
            name = "Bastidor con dos estantes",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_ARMARIO_ABIERTO to ItemWithLimits(
            id = "bastidor_con_armario_abierto",
            name = "Bastidor con armario abierto",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_ARMARIO_PUERTAS_ABATIBLES to ItemWithLimits(
            id = "bastidor_con_armario_puertas_abatibles",
            name = "Bastidor con armario puertas abatibles",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_ARMARIO_PUERTAS_CORREDERAS to ItemWithLimits(
            id = "bastidor_con_armario_puertas_correderas",
            name = "Bastidor con armario puertas correderas",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_CAJONERA_TRES_CAJONES to ItemWithLimits(
            id = "bastidor_con_cajonera_tres_cajones",
            name = "Bastidor con cajonera tres cajones",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_CON_CAJONERA_CUATRO_CAJONES to ItemWithLimits(
            id = "bastidor_con_cajonera_cuatro_cajones",
            name = "Bastidor con cajonera cuatro cajones",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        ),
        MesasModulos.BASTIDOR_PARA_FREGADERO_O_SENO to ItemWithLimits(
            id = "bastidor_para_fregadero_o_seno",
            name = "Bastidor para fregadero o seno",
            minQuantity = 0,
            maxQuantity = 10,
            initialQuantity = 0
        )
    )

    // Métodos auxiliares para mantener compatibilidad con código existente

    fun getMesaLimiteByDisplayName(displayName: String): Map<Int, LimiteTramo>? {
        return MesasTipos.values()
            .find { it.displayName == displayName }
            ?.let { MESAS_LIMITES[it] }
    }

    fun getItemWithLimitsForElementoGeneral(nombre: String): ItemWithLimits? {
        return MesasElementosGenerales.values()
            .find { it.displayName == nombre }
            ?.let { LIMITES_ELEMENTOS_GENERALES[it] }
    }

    fun getItemWithLimitsForCubeta(nombre: String): ItemWithLimits? {
        return MesasCubetas.values()
            .find { it.displayName == nombre }
            ?.let { LIMITES_CUBETAS[it] }
    }

    fun getItemWithLimitsForModulo(nombre: String): ItemWithLimits? {
        return MesasModulos.values()
            .find { it.displayName == nombre }
            ?.let { LIMITES_MODULOS[it] }
    }
}