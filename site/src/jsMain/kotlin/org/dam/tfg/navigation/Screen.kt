package org.dam.tfg.navigation

sealed class Screen(val route: String) {
    object Home: Screen(route = "/budget")
    object Login: Screen(route = "/login")
    object AdminHome: Screen(route = "/admin/")
    object AdminEdit: Screen(route = "/admin/edit")
    object AdminList: Screen(route = "/admin/list")

    object TableSelector: Screen(route = "/budget/table/table-selector-dimensions")
    object TableElements: Screen(route = "/budget/table/table-selector-elements")
    object TableCubetasSoporteBandejas: Screen(route = "/budget/table/table-selector-cubetas-soporte-bandejas")
}