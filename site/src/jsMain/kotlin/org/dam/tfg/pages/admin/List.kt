package org.dam.tfg.pages.admin

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AdminPageLayout
import org.dam.tfg.util.Constants.SIDE_PANEL_WIDTH
import org.dam.tfg.util.isAdminCheck
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.px
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY

@Page
@Composable
fun AdminListPage() {
    isAdminCheck {
        AdminListScreenContent()
    }
}

@Composable
fun AdminListScreenContent() {
    val breakpoint = rememberBreakpoint()
    AdminPageLayout {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .margin(topBottom = 50.px)
                .padding(left = if(breakpoint > Breakpoint.MD) SIDE_PANEL_WIDTH.px else 0.px),
            contentAlignment = Alignment.TopCenter
        ) {
            SpanText(
                text = "Aquí iría un listado de los presupuestos generados por los usuarios, ordenados por fecha.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(left = 20.px, right = 20.px, top = 20.px, bottom = 20.px)
                    .color(Theme.Red.rgb)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(40.px),

            )
        }
    }
}