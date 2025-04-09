package org.dam.tfg.resources

import org.dam.tfg.resources.ResourceProvider

class WebResourceProvider : ResourceProvider {
    override fun getImagePath(imageKey: String): String {
        return when (imageKey) {
            "CUBETA" -> "/form/cubeta.svg"
            "PETO_LATERAL" -> "/form/petoLateral.svg"
            "ESQUINA_EN_CHAFLAN" -> "/form/esquinaEnChaflan.svg"
            "ESQUINA_REDONDEADA" -> "/form/esquinaRedondeada.svg"
            "CAJEADO_COLUMNA" -> "/form/cajeadoColumna.svg"
            "ARO_DE_DESBRACE" -> "/form/arcoDeDesbrace.svg"
            "KIT_LAVAMANOS_PULSADOR" -> "/form/kitLavamanosPulsador.svg"
            "KIT_LAVAMANOS_PEDAL_SIMPLE" -> "/form/kitLavamanosPedalSimple.svg"
            "KIT_LAVAMANOS_PEDAL_DOBLE" -> "/form/kitLavamanosPedalDoble.svg"
            "BAQUETON_EN_SENO" -> "/form/baquetonEnSeno.svg"
            "BAQUETON_PERIMETRICO" -> "/form/baquetonPerimetrico.svg"

            /*

            "MESA_TRAMOS_1" -> "/form/mesaTramos1.svg"
            "MESA_TRAMOS_2" -> "/form/mesaTramos2.svg"
            "MESA_TRAMOS_3" -> "/form/mesaTramos3.svg"
            "MESA_TRAMOS_4" -> "/form/mesaTramos4.svg" */

            // Mesa 1 tramo (2 combinaciones)
            "MESA_1TRAMO_CENTRAL" -> "/form/mesa1TramoCentral.svg"
            "MESA_1TRAMO_MURAL"   -> "/form/mesa1TramoMural.svg"

            // Mesa 2 tramos (4 combinaciones)
            "MESA_2TRAMOS_CC" -> "/form/mesa2TramosCentral.svg"
            "MESA_2TRAMOS_CM" -> "/form/mesa2TramosCentralMural.svg"
            "MESA_2TRAMOS_MC" -> "/form/mesa2TramosMuralCentral.svg"
            "MESA_2TRAMOS_MM" -> "/form/mesa2TramosMural.svg"

            // Mesa 3 tramos (8 combinaciones)
            "MESA_3TRAMOS_CCC" -> "/form/mesa3TramosCentralCentralCentral.svg"
            "MESA_3TRAMOS_CCM" -> "/form/mesa3TramosCentralCentralMural.svg"
            "MESA_3TRAMOS_CMC" -> "/form/mesa3TramosCentralMuralCentral.svg"
            "MESA_3TRAMOS_CMM" -> "/form/mesa3TramosCentralMuralMural.svg"
            "MESA_3TRAMOS_MCC" -> "/form/mesa3TramosMuralCentralCentral.svg"
            "MESA_3TRAMOS_MCM" -> "/form/mesa3TramosMuralCentralMural.svg"
            "MESA_3TRAMOS_MMC" -> "/form/mesa3TramosMuralMuralCentral.svg"
            "MESA_3TRAMOS_MMM" -> "/form/mesa3TramosMuralMuralMural.svg"

            // Mesa 4 tramos (16 combinaciones)
            "MESA_4TRAMOS_CCCC" -> "/form/mesa4TramosCentralCentralCentralCentral.svg"
            "MESA_4TRAMOS_CCCM" -> "/form/mesa4TramosCentralCentralCentralMural.svg"
            "MESA_4TRAMOS_CCMC" -> "/form/mesa4TramosCentralCentralMuralCentral.svg"
            "MESA_4TRAMOS_CCMM" -> "/form/mesa4TramosCentralCentralMuralMural.svg"
            "MESA_4TRAMOS_CMCC" -> "/form/mesa4TramosCentralMuralCentralCentral.svg"
            "MESA_4TRAMOS_CMCM" -> "/form/mesa4TramosCentralMuralCentralMural.svg"
            "MESA_4TRAMOS_CMMC" -> "/form/mesa4TramosCentralMuralMuralCentral.svg"
            "MESA_4TRAMOS_CMMM" -> "/form/mesa4TramosCentralMuralMuralMural.svg"
            "MESA_4TRAMOS_MCCC" -> "/form/mesa4TramosMuralCentralCentralCentral.svg"
            "MESA_4TRAMOS_MCCM" -> "/form/mesa4TramosMuralCentralCentralMural.svg"
            "MESA_4TRAMOS_MCMC" -> "/form/mesa4TramosMuralCentralMuralCentral.svg"
            "MESA_4TRAMOS_MCMM" -> "/form/mesa4TramosMuralCentralMuralMural.svg"
            "MESA_4TRAMOS_MMCC" -> "/form/mesa4TramosMuralMuralCentralCentral.svg"
            "MESA_4TRAMOS_MMCM" -> "/form/mesa4TramosMuralMuralCentralMural.svg"
            "MESA_4TRAMOS_MMMC" -> "/form/mesa4TramosMuralMuralMuralCentral.svg"
            "MESA_4TRAMOS_MMMM" -> "/form/mesa4TramosMuralMuralMuralMural.svg"



            else -> "/form/noSeleccionado.svg"
        }
    }
}