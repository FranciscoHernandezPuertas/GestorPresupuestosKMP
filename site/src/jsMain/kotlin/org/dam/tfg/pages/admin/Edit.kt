package org.dam.tfg.pages.admin

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import org.dam.tfg.components.AdminPageLayout
import org.dam.tfg.util.isAdminCheck
import org.dam.tfg.util.isUserLoggedInCheck

@Page
@Composable
fun AdminEditPage() {
    isAdminCheck {
        AdminEditScreenContent()
    }
}

@Composable
fun AdminEditScreenContent() {
    AdminPageLayout {  }
}