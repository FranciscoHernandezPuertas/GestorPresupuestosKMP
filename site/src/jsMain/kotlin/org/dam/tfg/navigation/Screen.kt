package org.dam.tfg.navigation

sealed class Screen(val route: String) {
    object Home: Screen(route = "/budget")
    object Login: Screen(route = "/login")
    object AdminHome: Screen(route = "/admin/")
    object AdminEdit: Screen(route = "/admin/edit")
    object AdminList: Screen(route = "/admin/list")
    object AdminHistory: Screen(route = "/admin/history")

    object TableSelectorDimensions: Screen(route = "/budget/table/table-selector-dimensions")
    object TableSelectorElements: Screen(route = "/budget/table/table-selector-elements")
    object TableSelectorCubetas: Screen(route = "/budget/table/table-selector-cubetas")
    object TableSelectorModules: Screen(route = "/budget/table/table-selector-modules")
    object TableSelectorResume: Screen(route = "/budget/table/table-selector-resume")
    object TableSelectorBudget: Screen(route = "/budget/table/table-selector-budget")
    object PdfGenerator: Screen(route = "/budget/pdf-generator")
}