package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
actual enum class MesasTipos {
    TRAMOS_1, TRAMOS_2, TRAMOS_3, TRAMOS_4;

    actual val displayName: String
        get() = when (this) {
            TRAMOS_1 -> "1 Tramo"
            TRAMOS_2 -> "2 Tramos"
            TRAMOS_3 -> "3 Tramos"
            TRAMOS_4 -> "4 Tramos"
        }
}
@Serializable
actual enum class MesasElementosGenerales {
    PETO_LATERAL, KIT_LAVAMANOS_PULSADOR, ESQUINA_CHAFLAN, KIT_LAVAMANOS_PEDAL_SIMPLE, ESQUINA_REDONDEADA, KIT_LAVAMANOS_PEDAL_DOBLE, CAJEADO_COLUMNA, BAQUETON_SENO, ARO_DESBARACE, BAQUETON_PERIMETRICO;

    actual val displayName: String
        get() = when (this) {
            PETO_LATERAL -> "Peto lateral"
            KIT_LAVAMANOS_PULSADOR -> "Kit lavamanos pulsador"
            ESQUINA_CHAFLAN -> "Esquina chaflán"
            KIT_LAVAMANOS_PEDAL_SIMPLE -> "Kit lavamanos pedal simple"
            ESQUINA_REDONDEADA -> "Esquina redondeada"
            KIT_LAVAMANOS_PEDAL_DOBLE -> "Kit lavamanos pedal doble"
            CAJEADO_COLUMNA -> "Cajeado columna"
            BAQUETON_SENO -> "Baquetón seno"
            ARO_DESBARACE -> "Aro desbarace"
            BAQUETON_PERIMETRICO -> "Baquetón perimétrico"
        }
}

@Serializable
actual enum class MesasCubetas {
    DIAMETRO_300X180, DIAMETRO_360X180, DIAMETRO_380X180, DIAMETRO_420X180, DIAMETRO_460X180, CUADRADA_400X400X250, CUADRADA_400X400X300, CUADRADA_450X450X250, CUADRADA_450X450X300, CUADRADA_500X500X250, CUADRADA_500X500X300, RECTANGULAR_325X300X150, RECTANGULAR_500X300X300, RECTANGULAR_500X400X250, RECTANGULAR_600X450X300, RECTANGULAR_600X500X250, RECTANGULAR_600X500X300, RECTANGULAR_600X500X320, RECTANGULAR_630X510X380, RECTANGULAR_700X450X350, RECTANGULAR_800X500X380, RECTANGULAR_955X510X380, RECTANGULAR_1280X510X380;

    actual val displayName: String
        get() = when (this) {
            DIAMETRO_300X180 -> "Diámetro 300x180"
            DIAMETRO_360X180 -> "Diámetro 360x180"
            DIAMETRO_380X180 -> "Diámetro 380x180"
            DIAMETRO_420X180 -> "Diámetro 420x180"
            DIAMETRO_460X180 -> "Diámetro 460x180"
            CUADRADA_400X400X250 -> "Cuadrada 400x400x250"
            CUADRADA_400X400X300 -> "Cuadrada 400x400x300"
            CUADRADA_450X450X250 -> "Cuadrada 450x450x250"
            CUADRADA_450X450X300 -> "Cuadrada 450x450x300"
            CUADRADA_500X500X250 -> "Cuadrada 500x500x250"
            CUADRADA_500X500X300 -> "Cuadrada 500x500x300"
            RECTANGULAR_325X300X150 -> "Rectangular 325x300x150"
            RECTANGULAR_500X300X300 -> "Rectangular 500x300x300"
            RECTANGULAR_500X400X250 -> "Rectangular 500x400x250"
            RECTANGULAR_600X450X300 -> "Rectangular 600x450x300"
            RECTANGULAR_600X500X250 -> "Rectangular 600x500x250"
            RECTANGULAR_600X500X300 -> "Rectangular 600x500x300"
            RECTANGULAR_600X500X320 -> "Rectangular 600x500x320"
            RECTANGULAR_630X510X380 -> "Rectangular 630x510x380"
            RECTANGULAR_700X450X350 -> "Rectangular 700x450x350"
            RECTANGULAR_800X500X380 -> "Rectangular 800x500x380"
            RECTANGULAR_955X510X380 -> "Rectangular 955x510x380"
            RECTANGULAR_1280X510X380 -> "Rectangular 1280x510x380"
        }
}

@Serializable
actual enum class MesasModulos {
    BASTIDOR_SIN_ESTANTE, BASTIDOR_CON_ESTANTE, BASTIDOR_CON_DOS_ESTANTES, BASTIDOR_CON_ARMARIO_ABIERTO, BASTIDOR_CON_ARMARIO_PUERTAS_ABATIBLES, BASTIDOR_CON_ARMARIO_PUERTAS_CORREDERAS, BASTIDOR_CON_CAJONERA_TRES_CAJONES, BASTIDOR_CON_CAJONERA_CUATRO_CAJONES, BASTIDOR_PARA_FREGADERO_O_SENO;

    actual val displayName: String
        get() = when (this) {
            BASTIDOR_SIN_ESTANTE -> "Bastidor sin estante"
            BASTIDOR_CON_ESTANTE -> "Bastidor con estante"
            BASTIDOR_CON_DOS_ESTANTES -> "Bastidor con dos estantes"
            BASTIDOR_CON_ARMARIO_ABIERTO -> "Bastidor con armario abierto"
            BASTIDOR_CON_ARMARIO_PUERTAS_ABATIBLES -> "Bastidor con armario puertas abatibles"
            BASTIDOR_CON_ARMARIO_PUERTAS_CORREDERAS -> "Bastidor con armario puertas correderas"
            BASTIDOR_CON_CAJONERA_TRES_CAJONES -> "Bastidor con cajonera tres cajones"
            BASTIDOR_CON_CAJONERA_CUATRO_CAJONES -> "Bastidor con cajonera cuatro cajones"
            BASTIDOR_PARA_FREGADERO_O_SENO -> "Bastidor para fregadero o seno"
        }
}

@Serializable
actual enum class TipoTramo {
    CENTRAL, MURAL;

    actual val displayName: String
        get() = when (this) {
            CENTRAL -> "Central"
            MURAL -> "Mural"
        }
}