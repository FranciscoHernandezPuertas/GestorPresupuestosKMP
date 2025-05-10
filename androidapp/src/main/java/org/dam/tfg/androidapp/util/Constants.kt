package org.dam.tfg.androidapp.util

object Constants {
    const val DATABASE_NAME = "gestor_db"
    
    // Navigation routes
    const val ROUTE_LOGIN = "login"
    const val ROUTE_HOME = "home"
    const val ROUTE_EDIT = "edit"
    const val ROUTE_LIST = "list"
    const val ROUTE_HISTORY = "history"
    const val ROUTE_LOGOUT = "logout"
    
    // Edit sub-routes
    const val ROUTE_EDIT_MATERIALS = "edit/materials"
    const val ROUTE_EDIT_FORMULAS = "edit/formulas"
    const val ROUTE_EDIT_USERS = "edit/users"
    
    // Shared preferences
    const val PREF_NAME = "admin_panel_prefs"
    const val PREF_TOKEN = "user_token"
    const val PREF_USERNAME = "username"
    const val PREF_USER_TYPE = "user_type"
}
