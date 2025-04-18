package org.dam.tfg.resources

import org.dam.tfg.resources.ResourceProvider

class WebResourceProvider : ResourceProvider {
    override fun getImagePath(imageKey: String): String {
        return when (imageKey) {
            "CUBETA" -> "/form/table/elements/cubeta.svg"
            "PETO_LATERAL" -> "/form/table/elements/petoLateral.svg"
            "ESQUINA_EN_CHAFLAN" -> "/form/table/elements/esquinaEnChaflan.svg"
            "ESQUINA_REDONDEADA" -> "/form/table/elements/esquinaRedondeada.svg"
            "CAJEADO_COLUMNA" -> "/form/table/elements/cajeadoColumna.svg"
            "ARO_DE_DESBARACE" -> "/form/table/elements/arcoDeDesbarace.svg"
            "KIT_LAVAMANOS_PULSADOR" -> "/form/table/elements/kitLavamanosPulsador.svg"
            "KIT_LAVAMANOS_PEDAL_SIMPLE" -> "/form/table/elements/kitLavamanosPedalSimple.svg"
            "KIT_LAVAMANOS_PEDAL_DOBLE" -> "/form/table/elements/kitLavamanosPedalDoble.svg"
            "BAQUETON_EN_SENO" -> "/form/table/elements/baquetonEnSeno.svg"
            "BAQUETON_PERIMETRICO" -> "/form/table/elements/baquetonPerimetrico.svg"

            // Mesa 1 tramo (2 combinaciones)
            "MESA_1TRAMO_CENTRAL" -> "/form/table/types/mesa1TramoCentral.svg"
            "MESA_1TRAMO_MURAL"   -> "/form/table/types/mesa1TramoMural.svg"

            // Mesa 2 tramos (4 combinaciones)
            "MESA_2TRAMOS_CC" -> "/form/table/types/mesa2TramosCentral.svg"
            "MESA_2TRAMOS_CM" -> "/form/table/types/mesa2TramosCentralMural.svg"
            "MESA_2TRAMOS_MC" -> "/form/table/types/mesa2TramosMuralCentral.svg"
            "MESA_2TRAMOS_MM" -> "/form/table/types/mesa2TramosMural.svg"

            // Mesa 3 tramos (8 combinaciones)
            "MESA_3TRAMOS_CCC" -> "/form/table/types/mesa3TramosCentralCentralCentral.svg"
            "MESA_3TRAMOS_CCM" -> "/form/table/types/mesa3TramosCentralCentralMural.svg"
            "MESA_3TRAMOS_CMC" -> "/form/table/types/mesa3TramosCentralMuralCentral.svg"
            "MESA_3TRAMOS_CMM" -> "/form/table/types/mesa3TramosCentralMuralMural.svg"
            "MESA_3TRAMOS_MCC" -> "/form/table/types/mesa3TramosMuralCentralCentral.svg"
            "MESA_3TRAMOS_MCM" -> "/form/table/types/mesa3TramosMuralCentralMural.svg"
            "MESA_3TRAMOS_MMC" -> "/form/table/types/mesa3TramosMuralMuralCentral.svg"
            "MESA_3TRAMOS_MMM" -> "/form/table/types/mesa3TramosMuralMuralMural.svg"

            // Mesa 4 tramos (16 combinaciones)
            "MESA_4TRAMOS_CCCC" -> "/form/table/types/mesa4TramosCentralCentralCentralCentral.svg"
            "MESA_4TRAMOS_CCCM" -> "/form/table/types/mesa4TramosCentralCentralCentralMural.svg"
            "MESA_4TRAMOS_CCMC" -> "/form/table/types/mesa4TramosCentralCentralMuralCentral.svg"
            "MESA_4TRAMOS_CCMM" -> "/form/table/types/mesa4TramosCentralCentralMuralMural.svg"
            "MESA_4TRAMOS_CMCC" -> "/form/table/types/mesa4TramosCentralMuralCentralCentral.svg"
            "MESA_4TRAMOS_CMCM" -> "/form/table/types/mesa4TramosCentralMuralCentralMural.svg"
            "MESA_4TRAMOS_CMMC" -> "/form/table/types/mesa4TramosCentralMuralMuralCentral.svg"
            "MESA_4TRAMOS_CMMM" -> "/form/table/types/mesa4TramosCentralMuralMuralMural.svg"
            "MESA_4TRAMOS_MCCC" -> "/form/table/types/mesa4TramosMuralCentralCentralCentral.svg"
            "MESA_4TRAMOS_MCCM" -> "/form/table/types/mesa4TramosMuralCentralCentralMural.svg"
            "MESA_4TRAMOS_MCMC" -> "/form/table/types/mesa4TramosMuralCentralMuralCentral.svg"
            "MESA_4TRAMOS_MCMM" -> "/form/table/types/mesa4TramosMuralCentralMuralMural.svg"
            "MESA_4TRAMOS_MMCC" -> "/form/table/types/mesa4TramosMuralMuralCentralCentral.svg"
            "MESA_4TRAMOS_MMCM" -> "/form/table/types/mesa4TramosMuralMuralCentralMural.svg"
            "MESA_4TRAMOS_MMMC" -> "/form/table/types/mesa4TramosMuralMuralMuralCentral.svg"
            "MESA_4TRAMOS_MMMM" -> "/form/table/types/mesa4TramosMuralMuralMuralMural.svg"

            // Módulos
            "BASTIDOR_CON_ARMARIO_ABIERTO" -> "/form/table/modules/bastidorConArmarioAbierto.svg"
            "BASTIDOR_CON_ARMARIO_PUERTAS_ABATIBLES" -> "/form/table/modules/bastidorConArmarioPuertasAbatibles.svg"
            "BASTIDOR_CON_ARMARIO_PUERTAS_CORREDERAS" -> "/form/table/modules/bastidorConArmarioPuertasCorrederas.svg"
            "BASTIDOR_CON_CAJONERA_CUATRO_CAJONES" -> "/form/table/modules/bastidorConCajoneraCuatroCajones.svg"
            "BASTIDOR_CON_CAJONERA_TRES_CAJONES" -> "/form/table/modules/bastidorConCajoneraTresCajones.svg"
            "BASTIDOR_CON_DOS_ESTANTES" -> "/form/table/modules/bastidorConDosEstantes.svg"
            "BASTIDOR_CON_ESTANTE" -> "/form/table/modules/bastidorConEstante.svg"
            "BASTIDOR_PARA_FREGADERO_O_SENO" -> "/form/table/modules/bastidorParaFregaderoOSeno.svg"
            "BASTIDOR_SIN_ESTANTE" -> "/form/table/modules/bastidorSinEstante.svg"

            else -> "/form/noSeleccionado.svg"
        }
    }
}