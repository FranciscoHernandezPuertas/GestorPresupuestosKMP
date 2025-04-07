package org.dam.tfg.resources

interface ResourceProvider {
    fun getImagePath(imageKey: String): String
}
