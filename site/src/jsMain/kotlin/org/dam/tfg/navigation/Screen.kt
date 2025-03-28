package org.dam.tfg.navigation

sealed class Screen(val route: String) {
    object Home: Screen(route = "/")
    object Login: Screen(route = "/login")
    object AdminHome: Screen(route = "/admin/")
    object AdminEdit: Screen(route = "/admin/edit")
    object AdminList: Screen(route = "/admin/list")

    object TableSelector: Screen(route = "/budget/table/table-selector")
    object TableElements: Screen(route = "/budget/table/table-elements")
}