package org.dam.tfg.pages.admin

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import org.dam.tfg.components.AdminPageLayout
import org.dam.tfg.util.isUserLoggedInCheck

@Page
@Composable
fun AdminListPage() {
    isUserLoggedInCheck {
        AdminListScreenContent()
    }
}

@Composable
fun AdminListScreenContent() {
    AdminPageLayout {

    }
}