package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page
import org.dam.tfg.components.AppHeader
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.isUserLoggedInCheck

@Page
@Composable
fun TableSelectorModulesPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            // Contenido principal separado del header
            TableSelectorModulesContent()

        }
    }
}

@Composable
fun TableSelectorModulesContent() {

}