package org.dam.tfg.navigation

sealed class Screen(val route: String) {
    object AdminHome: Screen(route = "/admin/")
    object AdminLogin: Screen(route = "/admin/login")
    object AdminEdit: Screen(route = "/admin/edit")
    object AdminList: Screen(route = "/admin/list")
}