package org.dam.tfg.pages.admin

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.core.Page
import org.dam.tfg.components.SidePanel
import org.dam.tfg.util.Constants.PAGE_WIDTH
import org.dam.tfg.util.isUserLoggedIn
import org.jetbrains.compose.web.css.px

@Page
@Composable
fun HomeScreen() {
    isUserLoggedIn {
        HomePage()
    }
}

@Composable
fun HomePage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .maxWidth(PAGE_WIDTH.px)
        ) {
            SidePanel()
        }
    }
}