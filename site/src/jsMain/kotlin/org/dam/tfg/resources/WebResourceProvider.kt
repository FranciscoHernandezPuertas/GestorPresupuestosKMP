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
            "MESA_TRAMOS_1" -> "/form/mesaTramos1.svg"
            "MESA_TRAMOS_2" -> "/form/mesaTramos2.svg"
            "MESA_TRAMOS_3" -> "/form/mesaTramos3.svg"
            "MESA_TRAMOS_4" -> "/form/mesaTramos4.svg"

            else -> "/form/noSeleccionado.svg"
        }
    }
}