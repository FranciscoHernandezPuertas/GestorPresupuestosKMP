package org.dam.tfg.navigation

sealed class Screen(val route: String) {
    object Home: Screen(route = "/")
    object Login: Screen(route = "/login")
    object AdminHome: Screen(route = "/admin/")
    object AdminEdit: Screen(route = "/admin/edit")
    object AdminList: Screen(route = "/admin/list")
}